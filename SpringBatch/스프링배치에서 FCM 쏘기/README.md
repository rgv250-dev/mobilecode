BLE 통신을 이용하여 적립 방식 비슷하게 구현해봄

처음에는 통신 이후 등록된 데이터를 가져오는 것으로 생각하였는데, 아무래도 토스에 클릭 시 데이터가 굉장히 빠르게 반응 된것을 보니

단순히 어떤 값과 내부에서 쓰는 토큰이나 사용자에 대한 데이터를 기준으로 해서 통신해서 포인트를 적립하는걸로 보인다.
그래서 대충 데이터가 나오면 클릭 이후 서버 통신 이후 적립 처리 하면 될것이라고 판단하였고, 실제로도 그런지는 모르겠지만 성공하였다.

그 후에는 문제는 이미 받은 사용자에 대한 중복 처리라던지 통신이 있지만 테스트 시점에서는 쓰지 않았다.

그리고 추가로   //안드로이드에서 넘어오는 값이 hex라 String 으로 변환 참조 하기 바란다.
혹시 데이터를 못받거나 하는 경우에는 이런거가 문제였다... 

내부 DB에 저장하여 마지막 날짜값을 기준으로 화면에 값 보여줄때 제외하고 보여주도록 하여 처리하였다
통신 부분은 rest API 통신 이다.  

이정도면 큰 틀에서 나머지 정리해서 서비스 코드로 변경하면 된다.


서비스 주소
```
import UIKit
import CoreBluetooth
import Lottie
import RxSwift
import CoreLocation
import Toast_Swift

class BlePointViewController: UIViewController, StateDelegate, CustomPopupViewDelegate {
    
    func customPopupViewExtension(sender: LinkBottomAlert, didSelectNumber: Int) {
        dismiss(animated: true)
        if didSelectNumber == 1
        {
            debugPrint("Custom Popup Dismiss On Done Button Action")
        }
    }
    
    
    var peripherals: [CBPeripheral] = []
    var centralManager: CBCentralManager!
    
    let subjectBleData = PublishSubject<String>()
    let scanFilterServiceUUID = CBUUID.init(string:"0000b81d-0000-1000-8000-00805f9b34fb")
    
    private let userData = 기타 등등 여기에 추가 가져와서 추가하면 됨
    
    var peripheralManager: CBPeripheralManager!
    
    var service: CBMutableService!
    var handsCharacteristic: CBMutableCharacteristic!
        
    var divceData =  Array<String>()
    
    @IBOutlet weak var userIcon: AnimationSubview!
    
    @IBOutlet weak var userinfoCell01: BleInfoView!
    @IBOutlet weak var userinfoCell02: BleInfoView!
    @IBOutlet weak var userinfoCell03: BleInfoView!
    @IBOutlet weak var userinfoCell04: BleInfoView!
    @IBOutlet weak var button: UIButton!
    
    var disposBag = DisposeBag()
    
    func didUpdateState(_ state: CodeState, idData: String) {
        self.view.makeToast("서버에 데이터 보내고 100원 적립 클릭 한 아이디 \(idData)", duration: 1.0, position: .bottom)
        if let index = divceData.firstIndex(of: idData) {
            divceData.remove(at: index)
            print("\(idData) 데이터가 성공적으로 삭제되었습니다.")
        }
    }
    
    @IBAction func buttonEvent(_ sender: Any) {
        DispatchQueue.main.async {
            guard let popupViewController = LinkBottomAlert.instantiate() else { return }
            popupViewController.delegate = self
            popupViewController.data = "데이터 어쩌고저쩌고"
            
            let popupVC = PopupViewController(contentController: popupViewController, position: .bottom(0), popupWidth: self.view.frame.width, popupHeight: 216)
            popupVC.cornerRadius = 15
            popupVC.backgroundAlpha = 0.0
            popupVC.backgroundColor = .clear
            popupVC.canTapOutsideToDismiss = true
            popupVC.shadowEnabled = true
            popupVC.delegate = self
            popupVC.modalPresentationStyle = .popover
            self.present(popupVC, animated: true, completion: nil)
        }
    }
    
    
    @IBAction func close(_ sender: Any) {
        dismiss(animated: false)
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        centralManager = CBCentralManager(delegate: self, queue: nil, options: [CBCentralManagerOptionShowPowerAlertKey: true])
        centralManager.delegate = self
        
        self.peripheralManager = CBPeripheralManager(delegate: self, queue: nil, options: [CBPeripheralManagerOptionShowPowerAlertKey: true])
        self.peripheralManager.delegate = self
        
        
        let userAnimaiton = LottieAnimationView(name: "user_icon")
        userIcon.addSubview(userAnimaiton)
        userAnimaiton.contentMode = .scaleAspectFit
        userAnimaiton.center = self.view.center
        userAnimaiton.frame = CGRect(x: 0, y: 0, width: 40, height: 40)
        userAnimaiton.loopMode = .loop
        userAnimaiton.play()
        
        
        self.userinfoCell01.delegate = self
        self.userinfoCell02.delegate = self
        self.userinfoCell03.delegate = self
        self.userinfoCell04.delegate = self
        
        
        subjectBleData.subscribe { event in
            print(event)
            
            var checkData = self.divceData.contains(event)
            print(checkData)
            if (!checkData && self.divceData.count < 4){
                self.divceData.append(event)
                for i in 0...self.divceData.count{
                    self.checkViewSet(val: event)
                }
            }
        
        }.disposed(by: self.disposBag)

    }
    func startScanBle(){
        if(!centralManager.isScanning){
            centralManager?.scanForPeripherals(withServices: [scanFilterServiceUUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: true])
        }
    }
    
    
    func startAdvertising(){
        
        let service = CBMutableService(type: scanFilterServiceUUID, primary: true)
        self.peripheralManager.add(service)
        
        if (self.peripheralManager.isAdvertising){
            self.peripheralManager.stopAdvertising()
        }else{
            self.peripheralManager.startAdvertising([
                CBAdvertisementDataServiceUUIDsKey: [scanFilterServiceUUID],
                CBAdvertisementDataLocalNameKey : userData
            ])
            print("BLE Server started advertising")
        }
        
    }
    
    func checkViewSet(val insertData : String){
        print("itema")
        print(self.divceData.count)
        for i in self.divceData.indices {
            switch i {
            case 0:
                print("Case 0: Do something")
                if (self.userinfoCell01.isHidden == true){
                    self.userinfoCell01.isHidden = false
                    self.userinfoCell01.otherUserTitle.text = insertData
                    self.userinfoCell01.idStringData = insertData
                }
                break
            case 1:
                print("Case 1: Do something else")
                if (self.userinfoCell02.isHidden == true){
                    self.userinfoCell02.isHidden = false
                    self.userinfoCell02.otherUserTitle.text = insertData
                    self.userinfoCell02.idStringData = insertData
                }
                break
            case 2:
                print("Case 2: Do another thing")
                if (self.userinfoCell03.isHidden == true){
                    self.userinfoCell03.isHidden = false
                    self.userinfoCell03.otherUserTitle.text = insertData
                    self.userinfoCell03.idStringData = insertData
                }
                break
            case 3:
                print("Case 3: Do something different")
                if (self.userinfoCell03.isHidden == true){
                    self.userinfoCell03.isHidden = false
                    self.userinfoCell03.otherUserTitle.text = insertData
                    self.userinfoCell03.idStringData = insertData
                }
                break
            default:
                print("Default case")
                break
            }
        }
    }

}


extension BlePointViewController: CBPeripheralDelegate, CBCentralManagerDelegate, CBPeripheralManagerDelegate, PopupViewControllerDelegate{
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .unknown:
            print("unknown")
        case .resetting:
            print("restting")
        case .unsupported:
            print("unsupported")
        case .unauthorized:
            print("unauthorized")
        case .poweredOff:
            print("power Off")
        case .poweredOn:
            print("CBPeripheralManager power on")
            startAdvertising()
        @unknown default:
            fatalError()
        }
    }
    
    

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .unknown:
            print("unknown")
        case .resetting:
            print("restting")
        case .unsupported:
            print("unsupported")
        case .unauthorized:
            print("unauthorized")
        case .poweredOff:
            print("power Off")
        case .poweredOn:
            print("power on")
            startScanBle()
        @unknown default:
            fatalError()
        }
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        
        for (key, value) in advertisementData {
            if key == CBAdvertisementDataServiceDataKey {
                let serviceData = value as! [CBUUID : NSData]
                for (uuid, data) in serviceData {
                    
                    let startindex = data.debugDescription.replacingOccurrences(of: "<", with: "")
                    let endIdex = startindex.debugDescription.replacingOccurrences(of: ">", with: "")
                    let androidSendData =  hexToStr(text: endIdex)
                    if (!self.divceData.contains(androidSendData)){
                        subjectBleData.onNext(androidSendData)
                    }
                   
                }
            }
            
            if key == CBAdvertisementDataLocalNameKey {
                var iosSendData  : String = value as? String ?? ""
                if (!iosSendData.isEmpty || iosSendData.count != 0){
                    subjectBleData.onNext(iosSendData)
                }
                
            }
        }
        
    }
   
    // 기기 연결가 연결되면 호출되는 메서드입니다.
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.delegate = self
    }
    
    // characteristic 검색에 성공 시 호출되는 메서드입니다.
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
    }
    
    // writeType이 .withResponse일 때, 블루투스 기기로부터의 응답이 왔을 때 호출되는 함수입니다.
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        // writeType이 .withResponse인 블루투스 기기로부터 응답이 왔을 때 필요한 코드를 작성합니다.
        // 쓸수 필요가 없음
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        guard let services = peripheral.services else { return }
        for service in services {
            print("Discovered service: \(service)")
            peripheral.discoverCharacteristics(nil, for: service)
        }
    }
    
    
    // 블루투스 기기의 신호 강도를 요청하는 peripheral.readRSSI()가 호출하는 함수입니다.
    func peripheral(_ peripheral: CBPeripheral, didReadRSSI RSSI: NSNumber, error: Error?) {
        
    }
    
    func hexToStr(text: String) -> String {
      //안드로이드에서 넘어오는 값이 hex라 String 으로 변환
      let regex = try! NSRegularExpression(pattern: "(0x)?([0-9A-Fa-f]{2})", options: .caseInsensitive)
      let textNS = text as NSString
      let matchesArray = regex.matches(in: textNS as String, options: [], range: NSMakeRange(0, textNS.length))
          
      let characters = matchesArray.map {
          Character(UnicodeScalar(UInt32(textNS.substring(with: $0.range(at: 2)), radix: 16)!)!)
      }

      return String(characters)
    }
    
    
}



```

```

import UIKit
import Lottie

public protocol StateDelegate: AnyObject {
    /// loggedIn상태인지 loggedOut 상태인지 표출
    func didUpdateState(_ state: CodeState, idData : String)
}

public enum CodeState {
    case confirmed //확인 된 상태
}


class BleInfoView: UIView {

    @IBOutlet var userInfoMain: UIView!
    @IBOutlet weak var otherUserIcon: AnimationSubview!
    @IBOutlet weak var otherUserTitle: UILabel!
    var idStringData = ""

    let userAnimaiton = LottieAnimationView(name: "ble_icon")
    
    var delegate: StateDelegate?
    
    override init(frame: CGRect) {
            super.init(frame: frame)
        }
        
        required init?(coder: NSCoder) {
            super.init(coder: coder)
            custominit()
    }
    
    override func awakeFromNib() {
            super.awakeFromNib()
            

        otherUserIcon.addSubview(userAnimaiton)
        self.userAnimaiton.contentMode = .scaleAspectFit
        self.userAnimaiton.center = self.otherUserIcon.center
        self.userAnimaiton.frame = CGRect(x: 0, y: 0, width: 25, height: 25)
        //userAnimaiton.loopMode = .loop
        //userAnimaiton.play()
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(tabEvent))
        userInfoMain.addGestureRecognizer(tap)
    }
    
    @objc func tabEvent(){
        print("111112314213")
        self.userAnimaiton.play()
        print("aaaa id  : \(idStringData)")
        userAnimaiton.play { (finished) in
            self.userInfoMain.isHidden = true
            self.delegate?.didUpdateState(.confirmed, idData: self.idStringData)
        }
    }
    
    func custominit() {
           if let view = Bundle.main.loadNibNamed("BleInfoView", owner: self, options: nil)?.first as? UIView {
               view.frame = self.bounds
               addSubview(view)
           }
       }
}


```

