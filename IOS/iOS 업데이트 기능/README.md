iOS 앱 업데이트 간의형으로 확인하는 방법

itunes.apple.com에서 자기앱 아이디를 알아야함 

다른 작업이 필요하지 않아서 URLSession로만 작업됨

버전 정보를 비교해서 현재 앱 버전이 낮은 경우 알럿을 띄우는 기능임

전체 소스 시작

Model 소스

```

import Foundation

struct AppUpdateModel {
    static let appId = "" //자기 앱 ID를 찾아서 등록해야함 (매우중요)
    static let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
    static let buildNumber = Bundle.main.infoDictionary?["CFBundleVersion"] as? String
    static let appStoreUrlString = "itms-apps://itunes.apple.com/app/apple-store/\(AppUpdateModel.appId)"
    
    static func fetchLatestVersion(completion: @escaping (Result<String, Error>) -> Void) {
        guard let url = URL(string: "http://itunes.apple.com/lookup?id=\(appId)&country=kr") else {
            return completion(.failure(NSError(domain: "Invalid URL", code: 0, userInfo: nil)))
        }
        
        let task = URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data else {
                return completion(.failure(NSError(domain: "No Data", code: 0, userInfo: nil)))
            }
            
            do {
                if let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as? [String: Any],
                   let results = json["results"] as? [[String: Any]],
                   let latestVersion = results.first?["version"] as? String {
                    completion(.success(latestVersion))
                } else {
                    completion(.failure(NSError(domain: "Invalid JSON", code: 0, userInfo: nil)))
                }
            } catch {
                completion(.failure(error))
            }
        }
        task.resume()
    }
}


```

ViewModel 소스
```
import Foundation
import UIKit

class AppUpdateViewModel {
    private let model = AppUpdateModel()
    
    var onUpdateAvailable: ((String) -> Void)?
    var onError: ((String) -> Void)?
    
    func checkForUpdate() {
        AppUpdateModel.fetchLatestVersion { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success(let latestVersion):
                    if self?.isUpdateRequired(latestVersion: latestVersion) == true {
                        self?.onUpdateAvailable?(latestVersion)
                    }
                case .failure(let error):
                    self?.onError?(error.localizedDescription)
                }
            }
        }
    }
    
    private func isUpdateRequired(latestVersion: String) -> Bool {
        guard let currentVersion = AppUpdateModel.appVersion else { return false }
        
        let latestVersionComponents = latestVersion.split(separator: ".").compactMap { Int($0) }
        let currentVersionComponents = currentVersion.split(separator: ".").compactMap { Int($0) }
        
        for (latest, current) in zip(latestVersionComponents, currentVersionComponents) {
            if latest > current { return true }
            if latest < current { return false }
        }
        
        return latestVersionComponents.count > currentVersionComponents.count
    }
    
    func openAppStore() {
        guard let url = URL(string: AppUpdateModel.appStoreUrlString),
              UIApplication.shared.canOpenURL(url) else { return }
        UIApplication.shared.open(url, options: [:], completionHandler: nil)
    }
}

```

View
```
import UIKit

class ViewController: UIViewController {
    private let viewModel = AppUpdateViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupBindings()
        viewModel.checkForUpdate()
    }
    
    private func setupBindings() {
        viewModel.onUpdateAvailable = { [weak self] latestVersion in
            self?.showUpdateAlert(latestVersion: latestVersion)
        }
        
        viewModel.onError = { errorMessage in
            print("Error checking update: \(errorMessage)")
        }
    }
    
    private func showUpdateAlert(latestVersion: String) {
        let alert = UIAlertController(
            title: "업데이트 알림",
            message: "새로운 버전이 있습니다. \(latestVersion) 버전으로 업데이트 해주세요.",
            preferredStyle: .alert
        )
        
        let updateAction = UIAlertAction(title: "업데이트", style: .default) { [weak self] _ in
            self?.viewModel.openAppStore()
        }
        
        alert.addAction(updateAction)
        present(alert, animated: true, completion: nil)
    }
}


```


SwiftUI 형식으로 진행 

```

struct AppUpdateModel {
    static let appId = "" //자기 앱 ID를 찾아서 등록해야함 (매우중요)
    static let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
    static let buildNumber = Bundle.main.infoDictionary?["CFBundleVersion"] as? String
    static let appStoreUrlString = "itms-apps://itunes.apple.com/app/apple-store/\(AppUpdateModel.appId)"
    
    static func fetchLatestVersion(completion: @escaping (Result<String, Error>) -> Void) {
        guard let url = URL(string: "http://itunes.apple.com/lookup?id=\(appId)&country=kr") else {
            return completion(.failure(NSError(domain: "Invalid URL", code: 0, userInfo: nil)))
        }
        
        let task = URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data else {
                return completion(.failure(NSError(domain: "No Data", code: 0, userInfo: nil)))
            }
            
            do {
                if let json = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as? [String: Any],
                   let results = json["results"] as? [[String: Any]],
                   let latestVersion = results.first?["version"] as? String {
                    completion(.success(latestVersion))
                } else {
                    completion(.failure(NSError(domain: "Invalid JSON", code: 0, userInfo: nil)))
                }
            } catch {
                completion(.failure(error))
            }
        }
        task.resume()
    }
}


```


```
import Foundation
import SwiftUI

class AppUpdateViewModel: ObservableObject {
    @Published var isUpdateAvailable: Bool = false
    @Published var latestVersion: String?
    @Published var errorMessage: String?
    
    func checkForUpdate() {
        AppUpdateModel.fetchLatestVersion { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success(let latestVersion):
                    self?.latestVersion = latestVersion
                    self?.isUpdateAvailable = self?.isUpdateRequired(latestVersion: latestVersion) ?? false
                case .failure(let error):
                    self?.errorMessage = error.localizedDescription
                }
            }
        }
    }
    
    private func isUpdateRequired(latestVersion: String) -> Bool {
        guard let currentVersion = AppUpdateModel.appVersion else { return false }
        
        let latestVersionComponents = latestVersion.split(separator: ".").compactMap { Int($0) }
        let currentVersionComponents = currentVersion.split(separator: ".").compactMap { Int($0) }
        
        for (latest, current) in zip(latestVersionComponents, currentVersionComponents) {
            if latest > current { return true }
            if latest < current { return false }
        }
        
        return latestVersionComponents.count > currentVersionComponents.count
    }
    
    func openAppStore() {
        guard let url = URL(string: AppUpdateModel.appStoreUrlString),
              UIApplication.shared.canOpenURL(url) else { return }
        UIApplication.shared.open(url, options: [:], completionHandler: nil)
    }
}


```


```
import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = AppUpdateViewModel()
    
    var body: some View {
        VStack {
            if viewModel.isUpdateAvailable {
                Text("새로운 버전이 있습니다!")
                    .font(.title)
                    .foregroundColor(.red)
                    .padding()
                Button(action: {
                    viewModel.openAppStore()
                }) {
                    Text("업데이트 하기")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
            } else if let errorMessage = viewModel.errorMessage {
                Text("에러 발생: \(errorMessage)")
                    .foregroundColor(.red)
            } else {
                Text("앱이 최신 버전입니다.")
                    .font(.title)
                    .padding()
            }
        }
        .onAppear {
            viewModel.checkForUpdate()
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

```