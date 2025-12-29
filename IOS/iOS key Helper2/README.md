TCA 프로젝트 내부에서 쓸려고 만든것


```

//
//  AuthAPI.swift
//  something
//
//  Created by rgv250 on 12/29/25.
//

import Foundation
import Security
import Alamofire
import ComposableArchitecture

// MARK: - 토큰 모델

public struct AuthTokens: Codable, Equatable, Sendable {
  public var accessToken: String
  public var refreshToken: String
  public var expiresAt: Date

  public init(accessToken: String, refreshToken: String, expiresAt: Date) {
    self.accessToken = accessToken
    self.refreshToken = refreshToken
    self.expiresAt = expiresAt
  }

  /// 서버가 expiresIn(초)만 준다면 이 이니셜라이저로 써야함
  public init(accessToken: String, refreshToken: String, expiresIn: TimeInterval, now: Date = .now) {
    self.accessToken = accessToken
    self.refreshToken = refreshToken
    self.expiresAt = now.addingTimeInterval(expiresIn)
  }

  public func isExpiring(within seconds: TimeInterval, now: Date = .now) -> Bool {
    expiresAt.timeIntervalSince(now) <= seconds
  }
}

// MARK: - Errors

public enum AuthError: Error, Equatable {
  case notAuthenticated
  case refreshFailedPermanently
  case refreshResponseInvalid
  case httpStatus(Int)
}

// MARK: - Keychain

public struct KeychainStore: Sendable {
  public var loadData: @Sendable () throws -> Data?
  public var saveData: @Sendable (Data) throws -> Void
  public var delete: @Sendable () throws -> Void

  public static func live(
    service: String = Bundle.main.bundleIdentifier ?? "example.test",
    account: String = "auth.tokens.v1",
    accessible: CFString = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
  ) -> KeychainStore {

    func baseQuery() -> [CFString: Any] {
      [
        kSecClass: kSecClassGenericPassword,
        kSecAttrService: service,
        kSecAttrAccount: account
      ]
    }

    func keychainError(_ status: OSStatus) -> NSError {
      NSError(
        domain: "Keychain",
        code: Int(status),
        userInfo: [
          NSLocalizedDescriptionKey:
            (SecCopyErrorMessageString(status, nil) as String?) ?? "Keychain error \(status)"
        ]
      )
    }

    return KeychainStore(
      loadData: {
        var query = baseQuery()
        query[kSecReturnData] = true
        query[kSecMatchLimit] = kSecMatchLimitOne

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)

        if status == errSecItemNotFound { return nil }
        guard status == errSecSuccess else { throw keychainError(status) }
        return item as? Data
      },
      saveData: { data in
        var query = baseQuery()
        query[kSecAttrAccessible] = accessible

        let exists = SecItemCopyMatching(query as CFDictionary, nil)
        if exists == errSecSuccess {
          let attrs: [CFString: Any] = [kSecValueData: data]
          let status = SecItemUpdate(query as CFDictionary, attrs as CFDictionary)
          guard status == errSecSuccess else { throw keychainError(status) }
        } else {
          query[kSecValueData] = data
          let status = SecItemAdd(query as CFDictionary, nil)
          guard status == errSecSuccess else { throw keychainError(status) }
        }
      },
      delete: {
        let status = SecItemDelete(baseQuery() as CFDictionary)
        if status == errSecItemNotFound { return }
        guard status == errSecSuccess else { throw keychainError(status) }
      }
    )
  }
}

// MARK: - 토큰 저장 (인메모리+키체인 형태 보조)

public actor TokenStore {
  private let keychain: KeychainStore
  private var cached: AuthTokens?

  public init(keychain: KeychainStore = .live()) {
    self.keychain = keychain
  }

  public func load() throws -> AuthTokens? {
    if let cached { return cached }
    guard let data = try keychain.loadData() else { return nil }
    let tokens = try JSONDecoder().decode(AuthTokens.self, from: data)
    cached = tokens
    return tokens
  }

  public func save(_ tokens: AuthTokens) throws {
    let data = try JSONEncoder().encode(tokens)
    try keychain.saveData(data)
    cached = tokens
  }

  public func clear() throws {
    try keychain.delete()
    cached = nil
  }
}

// MARK: - Alamofire async wrapper

public struct HTTPClient: Sendable {
  public var data: @Sendable (URLRequest) async throws -> (Data, HTTPURLResponse)

  public static func alamofire(session: Session = .default) -> HTTPClient {
    HTTPClient { request in
      try await withCheckedThrowingContinuation { cont in
        session.request(request).responseData { response in
          if let error = response.error {
            cont.resume(throwing: error)
            return
          }
          guard let http = response.response, let data = response.data else {
            cont.resume(throwing: URLError(.badServerResponse))
            return
          }
          cont.resume(returning: (data, http))
        }
      }
    }
  }
}

// MARK: - Refresh 단일화 actor (파일 스코프에 있어야 캡처 에러 안 남)

public actor AuthRefresher {
  private var inflight: Task<AuthTokens, Error>?
  private var failCount: Int = 0

  private let http: HTTPClient
  private let tokenStore: TokenStore
  private let maxAttempts: Int
  private let makeRefreshRequest: @Sendable (String) throws -> URLRequest
  private let decodeRefreshResponse: @Sendable (Data) throws -> AuthTokens

  public init(
    http: HTTPClient,
    tokenStore: TokenStore,
    maxAttempts: Int,
    makeRefreshRequest: @escaping @Sendable (String) throws -> URLRequest,
    decodeRefreshResponse: @escaping @Sendable (Data) throws -> AuthTokens
  ) {
    self.http = http
    self.tokenStore = tokenStore
    self.maxAttempts = maxAttempts
    self.makeRefreshRequest = makeRefreshRequest
    self.decodeRefreshResponse = decodeRefreshResponse
  }

  public func refresh(currentRefreshToken: String) async throws -> AuthTokens {
    if let inflight { return try await inflight.value }

    // Task 내부에서는 actor 상태를 변경하지 않는다. 네트워크와 디코드만.
    let task = Task<AuthTokens, Error> {
      let req = try makeRefreshRequest(currentRefreshToken)
      let (data, resp) = try await http.data(req)

      guard (200..<300).contains(resp.statusCode) else {
        throw AuthError.httpStatus(resp.statusCode)
      }
      return try decodeRefreshResponse(data)
    }

    inflight = task
    defer { inflight = nil }

    do {
      let newTokens = try await task.value
      try await tokenStore.save(newTokens)
      failCount = 0
      return newTokens
    } catch {
      failCount += 1
      if failCount >= maxAttempts {
        throw AuthError.refreshFailedPermanently
      }
      throw error
    }
  }
}

// MARK: - AuthClient

public struct AuthClient: Sendable {
  /// 인증 붙여서 요청하고 Data를 돌려줌 (필요시 refresh + 1회 재시도 포함)
  public var authorizedData: @Sendable (URLRequest) async throws -> Data

  /// 외부에서 로그인/로그아웃 시 토큰 저장/삭제용
  public var saveTokens: @Sendable (AuthTokens) async throws -> Void
  public var clearTokens: @Sendable () async throws -> Void
  public var loadTokens: @Sendable () async throws -> AuthTokens?
}

// MARK: - Live implementation

extension AuthClient {
  /// refreshLeewaySeconds: 만료 n초 전 선제 refresh 60초
  /// maxRefreshAttempts: refresh 최대 시도 횟수 3회
  /// isTokenExpired: 서버가 바디 코드로 만료를 주는 경우 판정 로직 주입
  public static func live(
    http: HTTPClient = .alamofire(),
    tokenStore: TokenStore = TokenStore(),
    refreshLeewaySeconds: TimeInterval = 60,
    maxRefreshAttempts: Int = 3,
    makeRefreshRequest: @escaping @Sendable (_ refreshToken: String) throws -> URLRequest,
    decodeRefreshResponse: @escaping @Sendable (_ data: Data) throws -> AuthTokens,
    isTokenExpired: @escaping @Sendable (_ data: Data, _ response: HTTPURLResponse) -> Bool = { _, resp in
      // 기본값은 401만 만료로 봄
      resp.statusCode == 401
    }
  ) -> AuthClient {

    let refresher = AuthRefresher(
      http: http,
      tokenStore: tokenStore,
      maxAttempts: maxRefreshAttempts,
      makeRefreshRequest: makeRefreshRequest,
      decodeRefreshResponse: decodeRefreshResponse
    )

    func attachBearer(_ token: String, to request: URLRequest) -> URLRequest {
      var req = request
      var headers = req.allHTTPHeaderFields ?? [:]
      headers["Authorization"] = "Bearer \(token)"
      req.allHTTPHeaderFields = headers
      return req
    }

    func performOnce(_ request: URLRequest) async throws -> (Data, HTTPURLResponse) {
      try await http.data(request)
    }

    return AuthClient(
      authorizedData: { request in
        // 토큰 로드
        guard let tokens = try await tokenStore.load() else {
          throw AuthError.notAuthenticated
        }

        // 만료 60초 전이면 선제 refresh
        let usableTokens: AuthTokens
        if tokens.isExpiring(within: refreshLeewaySeconds) {
          usableTokens = try await refresher.refresh(currentRefreshToken: tokens.refreshToken)
        } else {
          usableTokens = tokens
        }

        // 요청 1회 수행
        let req1 = attachBearer(usableTokens.accessToken, to: request)
        let (data1, resp1) = try await performOnce(req1)
        if (200..<300).contains(resp1.statusCode) { return data1 }

        // 만료 판정(401 또는 서버 바디 코드)인 경우 refresh 후 1회 재시도
        if isTokenExpired(data1, resp1) {
          let current = try await tokenStore.load() ?? usableTokens
          let newTokens = try await refresher.refresh(currentRefreshToken: current.refreshToken)

          let req2 = attachBearer(newTokens.accessToken, to: request)
          let (data2, resp2) = try await performOnce(req2)
          guard (200..<300).contains(resp2.statusCode) else {
            throw AuthError.httpStatus(resp2.statusCode)
          }
          return data2
        }

        throw AuthError.httpStatus(resp1.statusCode)
      },
      saveTokens: { tokens in
        try await tokenStore.save(tokens)
      },
      clearTokens: {
        try await tokenStore.clear()
      },
      loadTokens: {
        try await tokenStore.load()
      }
    )
  }
}

// MARK: - TCA Dependency

private enum AuthClientKey: DependencyKey {
  static let liveValue: AuthClient = .live(
    makeRefreshRequest: { refreshToken in
      var req = URLRequest(url: URL(string: "https://example.com/auth/refresh")!)
      req.httpMethod = "POST"
      req.setValue("application/json", forHTTPHeaderField: "Content-Type")

      let body: [String: Any] = ["refreshToken": refreshToken]
      req.httpBody = try JSONSerialization.data(withJSONObject: body)
      return req
    },
    decodeRefreshResponse: { data in
      // 예) { "accessToken": "...", "refreshToken": "...", "expiresIn": 3600 }
      struct RefreshResponse: Decodable {
        var accessToken: String
        var refreshToken: String
        var expiresIn: Double
      }
      let decoded = try JSONDecoder().decode(RefreshResponse.self, from: data)
      return AuthTokens(
        accessToken: decoded.accessToken,
        refreshToken: decoded.refreshToken,
        expiresIn: decoded.expiresIn
      )
    },
    isTokenExpired: { data, resp in
      // TODO: 수정 되어야 할 수도 있음
      // 만료
      if resp.statusCode == 401 { return true }

      // 바디 코드로 만료 내려주는 케이스 (예시)
      struct APIErrorEnvelope: Decodable {
        let code: String?
        let errorCode: Int?
      }

      guard let env = try? JSONDecoder().decode(APIErrorEnvelope.self, from: data) else {
        return false
      }

      // 문자열 코드 예시
      if env.code == "TOKEN_EXPIRED" || env.code == "ACCESS_TOKEN_EXPIRED" {
        return true
      }

      // 숫자 코드 예시
      if env.errorCode == 1003 {
        return true
      }

      return false
    }
  )
}

public extension DependencyValues {
  var authClient: AuthClient {
    get { self[AuthClientKey.self] }
    set { self[AuthClientKey.self] = newValue }
  }
}



```
