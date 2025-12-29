//
//  Untitled.swift
//  serverTime
//
//  Created by suatecmac03 on 12/12/25.
//

import Foundation
import ComposableArchitecture

@Reducer
struct ClockFeature {
    // MARK: State
    @ObservableState
    struct State: Equatable {
        var utcDateText: String = ""
        var utcTimeText: String = ""
        var kstDateText: String = ""
        var kstTimeText: String = ""
    }

    // MARK: Action
    enum Action: Sendable {
        case onAppear
        case onDisappear
        case timerTicked
    }

    // MARK: Dependencies
    @Dependency(\.continuousClock) var clock    // 타이머용 Clock
    enum CancelID { case timer }

    // MARK: Reducer
    var body: some ReducerOf<Self> {
        Reduce { state, action in
            switch action {
            case .onAppear:
                // 처음 한 번 세팅
                Self.update(&state)

                // 1초마다 .timerTicked 날리는 타이머
                return .run { [clock] send in
                    for await _ in clock.timer(interval: .seconds(1)) {
                        await send(.timerTicked)
                    }
                }
                .cancellable(id: CancelID.timer)

            case .onDisappear:
                // 화면 나갈 때 타이머 캔슬
                return .cancel(id: CancelID.timer)

            case .timerTicked:
                Self.update(&state)
                return .none
            }
        }
    }

    // MARK: - 내부 헬퍼 (DateFormatter 포함)

    private static func update(_ state: inout State) {
        let now = Date()
        state.utcDateText = utcDateFormatter.string(from: now)
        state.utcTimeText = utcTimeFormatter.string(from: now)
        state.kstDateText = kstDateFormatter.string(from: now)
        state.kstTimeText = kstTimeFormatter.string(from: now)
    }

    private static let utcTZ = TimeZone(abbreviation: "UTC")!
    private static let kstTZ = TimeZone(identifier: "Asia/Seoul")!

    private static let utcDateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.timeZone = utcTZ
        f.dateFormat = "yyyy-MM-dd (EEE)"
        return f
    }()

    private static let utcTimeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.timeZone = utcTZ
        f.dateFormat = "HH:mm:ss"
        return f
    }()

    private static let kstDateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.timeZone = kstTZ
        f.dateFormat = "yyyy-MM-dd (EEE)"
        return f
    }()

    private static let kstTimeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.timeZone = kstTZ
        f.dateFormat = "HH:mm:ss"
        return f
    }()
}
