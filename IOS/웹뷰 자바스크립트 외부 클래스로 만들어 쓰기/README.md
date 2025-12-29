일단 웹뷰에서 연동시 자바 스크립트를 인식하여 작동하도록 처리 하도록 한다.

여러 방법은 있지만 여기서 필요한 것은 외부로 빼두는게 가장 좋다만 그러지 못하는 경우가 있다고 생각은 한다.
아니면 굳이 나도 잘 모르겠다. 더좋은 방법은 있을것이다 

```

///자바 스크립트 핸들러 클래스
class NavigationMessageHandler: NSObject, WKScriptMessageHandler {
    //메세지 및 핸들러 정의
    var messageHandlers: [String: (WKScriptMessage) -> Void] = [:]
    
    weak var viewController: ViewController?
    
    init(viewController: ViewController) {
        self.viewController = viewController
        super.init() // 기본 초기화 호출
        //메세지 핸들러 생성 추가시 아래에 값 추가
        messageHandlers  = [
            
            "appUpdate" : handleAppUpte,
            "keepScreen":handleKeepScreen
        ]
       
    }
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        
        print("userContentController: \(message.name)")
        if let handler = messageHandlers[message.name] {
            handler(message)
        } else {
            print("Unhandled message: \(message.name)")
        }
    }
   
    
    ///화면이 계속 켜져 있어야 하는 곳을 받고 켜두는 메소드
    private func handleKeepScreen(_ message: WKScriptMessage) {
        guard let messageBody = message.body as? String else { return }
        guard let viewController = self.viewController else {
                    print("viewController is nil")
                    return
                }
        viewController.viewModel.value = messageBody
    }
    
   
}

```

ViewController에서 설정 시 

```
class ViewController: UIViewController, NetworkCheckObserver, WKScriptMessageHandler {


let webConfiguration = WKWebViewConfiguration()
        webConfiguration.userContentController = WKUserContentController()
        navigationMessageHandler = NavigationMessageHandler(viewController: self)
        
        for (messageName, handler) in navigationMessageHandler.messageHandlers {
            webConfiguration.userContentController.add(self, name: messageName)
        }


```