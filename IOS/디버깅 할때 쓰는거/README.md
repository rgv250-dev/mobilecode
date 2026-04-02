디버깅용임

```

import os.log

struct AppDebugLogger {

    private static let tag = "AppDebugLogger"
    
    // 앱의 번들 식별자 등록 시 변경하면 됨
    private static let logger = OSLog(subsystem: Bundle.main.bundleIdentifier ?? "com.example.App", category: tag)
    
     //디버그 로그를 출력합니다.
    static func d(_ message: String) {
        #if DEBUG
        os_log("%@", log: logger, type: .debug, message)
        #endif
    }
    
    //정보 로그를 출력
    static func i(_ message: String) {
        #if DEBUG
        os_log("%@", log: logger, type: .info, message)
        #endif
    }
    
    //경고 로그를 출력합니다.
    static func w(_ message: String) {
        #if DEBUG
        os_log("%@", log: logger, type: .default, message)
        #endif
    }

    //에러 로그를 출력합니다.
    static func e(_ message: String) {
        #if DEBUG
        os_log("%@", log: logger, type: .error, message)
        #endif
    }

}

```
