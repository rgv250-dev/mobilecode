//가상 키보드값을 가져와서 크기를 맞춰 보자

//다른 곳을 클릭 했을때 가상 키보드 내리는 기능 추가 
extension UIViewController {
    func setupHideKeyboardOnTap() {
        self.view.addGestureRecognizer(self.endEditingRecognizer())
        self.navigationController?.navigationBar.addGestureRecognizer(self.endEditingRecognizer())
    }
    
    private func endEditingRecognizer() -> UIGestureRecognizer {
        let tap = UITapGestureRecognizer(target: self.view, action: #selector(self.view.endEditing(_:)))
        tap.cancelsTouchesInView = false
        return tap
    }
}

 @IBOutlet weak var collectionBottom: NSLayoutConstraint!

//viewDidLoad 에서 일단 이렇게 호출한다 
override func viewDidLoad() {
        super.viewDidLoad()
        self.setupHideKeyboardOnTap()

        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil )

  }

  

@objc func keyboardWillShow(notification: Notification) {
       
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
            let keyboardHeight = keyboardSize.height
            print(keyboardHeight)
            
            collectionBottom.constant = keyboardHeight - view.safeAreaInsets.bottom //키보드가 나타났을때 콜랙션 컨스트레이트 값 변경하여 컬랙션 뷰 크기를 줄여서 스크롤 되게 만들어줌 
        }
        
}
    
@objc func keyboardWillHide(notification: Notification) {
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
                   let keyboardHeight = keyboardSize.height
                   print(keyboardHeight)
                   
                   collectionBottom.constant = 0 //원 상태로 복구 시킴 
               }
}
