 SwiftUI로 페이지를 구성한 이유는 대부분 알겠지만 인증이라는게 로그인만 쓰이는것이면 크게 상관 없다.
근대 이래 저래 호출되는거면 차라리 뷰 위에 띄우는걸로 끝내고 꺼두는게 마음 편할거 같아서 구성하였다

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

호출 방법
```
    var swiftUIBioAuth = BioAuthView()
            swiftUIBioAuth.delegate = self
            let hostingController = UIHostingController(rootView: swiftUIBioAuth)
            hostingController.modalPresentationStyle = UIModalPresentationStyle.overFullScreen
            present(hostingController, animated: true, completion: nil) 
   
    
```

최종 상태값에 따른 확인법 

```

    func didUpdateState(_ state: AuthCodeState) {
        if case .confirmed = state {

        let token = keychainUtils.readItemsOnKeyChain(accountData: "키이름 비슷한거 이건 알아서 만드셔야하구요")
        //API 호출 후 Refresh Token만료 되어서 재발급 시키거나 다음 프로세스로 이동시면 된다.
            
        }else if case .unconfirmed = state {
           
        }else if case .lockdown = state {
            //생체 인증이 계속 실패해서 잠긴 상태
        }else if case .notWorking = state {
            
        }
        
    }


```
