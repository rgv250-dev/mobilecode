아이튠즈 서버 버전과 앱 버전을 비교해서 버전이 다른 경우 알럿창 생성하고 클릭시 앱 스토어로 이동시키는 코드
문제는 아이튠즈 서버에 JSON 파일 업데이트가 생각 보다 늦은 경우가 있어 완벽 하게는 작동하지 않음
보통은 서버에서 버전을 받아서 비교하는게 빠른대, 그러기에는 안드로이드에서는 인앱 업데이트가 사용하는 경우가 많아서,
굳이 하나를 위해서 서버를 만들어야 하나라는 생각에 만들어졌다.


```
/**
 * Created by rgv250-dev on 2024-01-10.
 * 어디 적당한 곳에 추가  
 * */
        AppUdate().getMarketVersion { result in
            switch result {
            case .failure(let error):
                print("failure getMarketVersion: \(error)")
            case .success(let value):
                if value.count > 0 {
                    let appStoreVersion = value[0]["version"] as! String
                    self.checkUpdateAlert(marketVersion: appStoreVersion)
                }
            }
}


```

   AppUdate 

```
   
   struct AppUdate {
    
    public static var APP_ID = "" //자신의 앱 버전아이디
    
    // 현재 버전 정보 : 타겟 -> 일반 -> Version
    static let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
    // 개발자가 내부적으로 확인하기 위한 용도 : 타겟 -> 일반 -> Build
    static let buildNumber = Bundle.main.infoDictionary?["CFBundleVersion"] as? String
    
    static let appStoreOpenUrlString = "itms-apps://itunes.apple.com/app/apple-store/\(AppUdate.APP_ID)"
    
    
    func latestVersion() -> String? {
        let appleID = AppUdate.APP_ID
        guard let url = URL(string: "http://itunes.apple.com/lookup?id=\(appleID)&country=kr"),
              let data = try? Data(contentsOf: url),
              let json = try? JSONSerialization.jsonObject(with: data, options: .allowFragments) as? [String: Any],
              let results = json["results"] as? [[String: Any]] else { return nil }
        
        if results.count > 0 {
            
            let appStoreVersion = results[0]["version"] as? String
            return appStoreVersion
            
        } else {
            return nil
        }
        
    }
    
    func getMarketVersion(completion: @escaping (Result<[[String:Any]],Error>) -> Void)  {
        
        let appleID = AppUdate.APP_ID
        guard let url = URL(string: "http://itunes.apple.com/lookup?id=\(appleID)&country=kr")else { return }
        
        
        AF.request(url, method: .get, parameters: nil, encoding:  URLEncoding.default, headers: nil)
            .responseData(completionHandler: { response in
                
                switch response.result {
                case .success(let data):
                    print("getMarketVersion in : \(data)")
                    let json = try? JSONSerialization.jsonObject(with: data, options: .allowFragments) as? [String: Any]
                    let results = json?["results"] as? [[String: Any]]
                    print("getMarketVersion in results: \(results)")
                    completion(.success(results ?? []))
                case let .failure(error):
                    print("getMarketVersion in error:\(error)")
                    completion(.failure(error))
                }
            })
    }
    
    // 앱 스토어로 이동 -> urlStr 에 appStoreOpenUrlString 넣으면 이동
    //
    func openAppStore() {
        guard let url = URL(string: AppUdate.appStoreOpenUrlString) else { return }
        if UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }
    
}

    
```


checkUpdateAlert 

```

    ///앱 버전 업데이트 얼럿
    private func checkUpdateAlert(marketVersion:String){
        
        let currentProjectVersion = AppUdate.appVersion!
        let splitMarketingVersion = marketVersion.split(separator: ".").map {$0}
        let splitCurrentProjectVersion = currentProjectVersion.split(separator: ".").map {$0}
        
        var marketingVersionString = ""
        for index in splitMarketingVersion {
            var indata : String  = String(index)
            marketingVersionString = marketingVersionString + indata
            
        }
        
        var appversionString = ""
        for index in splitCurrentProjectVersion {
            var indata : String  = String(index)
            appversionString = appversionString + indata
            
        }
        
        var marketingVersion : Int  = Int(marketingVersionString)!
        var appversion : Int = Int(appversionString)!
        
        print("markert Version : \(marketingVersion)")
        print("app Version : \(appversion)")
        
        if (marketingVersion > appversion){
            let alert = UIAlertController(title: "업데이트 알림", message: "새로운 버전이 있습니다. \(marketVersion) 버전으로 업데이트 해주세요.", preferredStyle: UIAlertController.Style.alert)
            let destructiveAction = UIAlertAction(title: "업데이트", style: UIAlertAction.Style.default){(_) in
                AppUdate().openAppStore()
            }
            alert.addAction(destructiveAction)
            self.present(alert, animated: false)
        }
        
    
    }


```
