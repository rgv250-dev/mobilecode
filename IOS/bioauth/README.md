iOS 생체 인증 이후 토큰을 이용한 로그인 또는 인증 방식 

보통의 생체 인증을 이용한 로그인이나 인증시에는 방법이 2가지이 있다. 

서버 통신을 이용하여 토큰과 리플래시 토큰을 미리 생성하여 저장 하여, 인증만 받아서 서비스를 진행하거나
또는 생체 인증이 끝난 후 생성된 값을 서버에 저장 하여 비교 이후 그 값을 기준으로 인증을 받는 방법이 있다.

하지만 보통의 앱 서비스라고 가정하면 전자로 구성되어 있는 경우가 많다. 

페이스 아이디 또는 터치 아이디로 인증을 받는 것을 SWiftUI로 구성하고 백그라운드는 투명 처리 한 후 인증이 완료가 되면, 현재 화면을 닫고 기능을 사용 하도록 하면 된다.

```

import SwiftUI
import LocalAuthentication

public protocol BioAuthenticateStateDelegate: AnyObject {
    /// loggedIn상태인지 loggedOut 상태인지 표출
    func didUpdateState(_ state: AuthCodeState)
}

public enum AuthCodeState {
    case confirmed //확인 된 상태
    case unconfirmed //확인 안된 상태
    case lockdown //생체 인증 잠금
    case notWorking //생체 인증이 적용 되지 않음
}


@available(iOS 15.0, *)
struct BioAuthView: View {
    public weak var delegate: BioAuthenticateStateDelegate?
    @Environment(\.dismiss) private var dismiss
    @State private var isAlart = false //이게 횟수 오버 일때 취소 버튼일 때
    let black : Color = Color(red: 0/255, green: 0/255, blue: 0/255)
    
    var body: some View {
    
        ZStack{
            //Spacer()
        }.background(BackgroundBlurView())
        .onAppear(
            perform: faceIdAuthentication
        )
    }
    
    func faceIdAuthentication(){
          let context = LAContext()
          var error: NSError?
          context.localizedFallbackTitle = ""
          
          if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error){
              let reason = "로그인을 위해 인증해주세요."
              context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason){ success, authenticationError in
                  if success{
                      Task{
                          try? await Task.sleep(nanoseconds: 125_000_000)
                          self.delegate?.didUpdateState(.confirmed)
                          await UIApplication.shared.windows.first?.rootViewController?.dismiss(animated: true, completion: {
                                                  
                          })
                      }
                      
                  }else{
                      print("failed")
                      DispatchQueue.main.async {
                          //self.delegate?.didUpdateState(.unconfirmed)
                          Task{
                              try? await Task.sleep(nanoseconds: 125_000_000)
                              self.delegate?.didUpdateState(.unconfirmed)
                              UIApplication.shared.windows.first?.rootViewController?.dismiss(animated: true, completion: {
                                                      
                              })
                          }
                      }
                      
                      let error = authenticationError! as NSError
                      let errorMessage = "\(error.code): \(error.localizedDescription)"
                      print(errorMessage)
                     
                    
                  }
              }
              
          }else{
              // Device does not support Face ID or Touch ID
              print("Biometric authentication unavailable")
              let errorMessage = "\(error!.code): \(error!.localizedDescription)"
              print(errorMessage)
              //Biometric authentication unavailable
              //-8: 생체 인식이 잠겨 있음
              
              if (error!.code == -7){
                  Task{
                      try? await Task.sleep(nanoseconds: 125_000_000)
                      self.delegate?.didUpdateState(.notWorking)
                      await UIApplication.shared.windows.first?.rootViewController?.dismiss(animated: true, completion: {
                                              
                      })
                  }
                  
              }else {
                  Task{
                      try? await Task.sleep(nanoseconds: 125_000_000)
                      self.delegate?.didUpdateState(.lockdown)
                      await UIApplication.shared.windows.first?.rootViewController?.dismiss(animated: true, completion: {
                                              
                      })
                  }
              }
              
             
          }
      }
    
}


struct BackgroundBlurView: UIViewRepresentable {
    func makeUIView(context: Context) -> UIView {
        let view = UIVisualEffectView(effect: UIBlurEffect(style: .light))
        DispatchQueue.main.async {
            view.superview?.superview?.backgroundColor = .clear
        }
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {}
}



#Preview {
    if #available(iOS 15.0, *) {
        BioAuth()
    } else {
        // Fallback on earlier versions
    }
}




```
호출 방법 UIViewController에서 swiftUI를 콜 하는 방법
```
    var swiftUIBioAuth = BioAuthView()
    swiftUIBioAuth.delegate = self
    let hostingController = UIHostingController(rootView: swiftUIBioAuth)
    hostingController.modalPresentationStyle = UIModalPresentationStyle.overFullScreen
    present(hostingController, animated: true, completion: nil) 
   
    
```


```

    func didUpdateState(_ state: AuthCodeState) {

        if case .confirmed = state {
        let token = keychainUtils.readItemsOnKeyChain(accountData: "키이름 비슷한거 이건 알아서 만드셔야하구요") //       
        //통신 후 토큰 만료 시 리플래시 토큰을 이용한 재발급 또는 작업 또는 다른 방법 소개
        }else if case .unconfirmed = state {
           //실패
        }else if case .lockdown = state {
         //생체 인증 실패로 잠김   
        }else if case .notWorking = state {
         //생체 인증 기능이 되어 있지 않는 상태
        }
        
    }

```
