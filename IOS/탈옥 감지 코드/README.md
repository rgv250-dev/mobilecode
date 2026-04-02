탈옥 확인용 코드

```

import UIKit
import Darwin

class JailbreakMessenger {

    //특정 탈옥 파일 존재 여부 확인
    private static func checkJailbreakFiles() -> Bool {
        let paths = [
            "/Applications/Cydia.app",
            "/Library/MobileSubstrate/MobileSubstrate.dylib",
            "/bin/bash",
            "/usr/sbin/sshd",
            "/etc/apt",
            "/private/var/lib/apt/"
        ]
        
        for path in paths {
            if FileManager.default.fileExists(atPath: path) {
                return true
            }
        }
        return false
    }

    //시스템 파일 쓰기 가능 여부 확인
    private static func canWriteToSystem() -> Bool {
        let testPath = "/private/jailbreakTest.txt"
        do {
            try "Jailbreak Test".write(toFile: testPath, atomically: true, encoding: .utf8)
            try FileManager.default.removeItem(atPath: testPath) // 테스트 파일 삭제
            return true
        } catch {
            return false
        }
    }

    //Cydia 등 탈옥 앱 실행 가능 여부 확인
    private static func canOpenCydia() -> Bool {
        // URL 스킴은 "cydia://"만 사용하도록 수정 (예시에서 패키지명을 제거)
        if let url = URL(string: "cydia://"), UIApplication.shared.canOpenURL(url) {
            return true
        }
        return false
    }
    
    //디버거 연결 여부 확인
    private static func isDebuggerAttached() -> Bool {
        var info = kinfo_proc()
        var mib: [Int32] = [CTL_KERN, KERN_PROC, KERN_PROC_PID, getpid()]
        var size = MemoryLayout<kinfo_proc>.size

        let result = sysctl(&mib, UInt32(mib.count), &info, &size, nil, 0)

        if result == 0, info.kp_proc.p_flag & P_TRACED != 0 {
            return true
        }
        return false
    }

 //쓰기 전용 디렉토리에서 읽기 모드에서 파일을 열 수 있는지 확인
    private static func canOpenFile() -> Bool {
        var file = fopen("/bin/bash", "r")
        if let file = file {
            fclose(file)
            return true
        }
        return false
    }

    
// 동적 라이브러리 목록을 순회하여 "frida" 관련 라이브러리가 로드되었는지 검사
//https://nightohl.tistory.com/entry/ios-탈옥탐지5-동적라이브러리-탐지 
private static func detectFridaInImages() -> Bool {
    let imageCount = _dyld_image_count()
    for i in 0..<imageCount {
        if let cImageName = _dyld_get_image_name(i) {
            let imageName = String(cString: cImageName).lowercased()
            if imageName.contains("frida") {
                return true
            }
        }
    }
    return false
}

// 실행 환경의 환경 변수를 검사하여 Frida 관련 값이 있는지 검사
private static func detectFridaInEnvironment() -> Bool {
    let environment = ProcessInfo.processInfo.environment
    if let dyldLibraries = environment["DYLD_INSERT_LIBRARIES"]?.lowercased() {
        if dyldLibraries.contains("frida") {
            return true
        }
    }
    return false
}

// 두 가지 방법을 종합하여 Frida의 존재 여부를 판단
private static func isFridaDetected() -> Bool {
    return detectFridaInImages() || detectFridaInEnvironment()
}

    // 모든 체크 항목을 조합하여 탈옥 여부 판단
    static func isDeviceJailbroken() -> Bool {
        return checkJailbreakFiles() ||
               canWriteToSystem() ||
               canOpenCydia() ||
               isDebuggerAttached() ||
               isFridaDetected
    }

    // 결과를 문자열로 제공 주로 디버그 시에 확인하기 위한 메소드
    static func getJailbreakStatusMessage() -> String {
        return isDeviceJailbroken() ? "탈옥된 기기입니다! 보안상 제한됩니다." : "안전한 기기입니다."
    }
 
}



/*ios에서 사용법 SwiftUI

import SwiftUI

@main
struct MyApp: App {
    
    init() {
        // 앱 초기화 시점에 탈옥 여부 검사
        if JailbreakMessenger.isDeviceJailbroken() {
            // 릴리즈 빌드에서는 앱 종료, 디버그에서는 로그 출력하도록 조건부 처리 가능
            #if !DEBUG
            exit(0)
            #else
            print("DEBUG: \(JailbreakMessenger.getJailbreakStatusMessage())")
            #endif
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                // 또는 ContentView 내부의 onAppear에서 추가 체크 가능
                .onAppear {
                    if JailbreakMessenger.isDeviceJailbroken() {
                        #if !DEBUG
                        exit(0)
                        #else
                        print("DEBUG: \(JailbreakMessenger.getJailbreakStatusMessage())")
                        #endif
                    }
                }
        }
    }
}
 

ios에서 사용법 구형태
import UIKit

class ViewController: UIViewController {

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        // 탈옥 여부 검사: 릴리즈 빌드에서는 앱 종료, 디버그 빌드에서는 로그 출력
        if JailbreakMessenger.isDeviceJailbroken() {
            #if !DEBUG
            exit(0)
            #else
            print("DEBUG: \(JailbreakMessenger.getJailbreakStatusMessage())")
            #endif
        }
    }
}*/

```
