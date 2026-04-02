


변경점

앱 켰을때 시간 기준으로 갱신 하여 화면에 뿌려주는 코드 중요한건 아래 뷰 모델 끝


```
final class TimeViewModel: ObservableObject {
    
    @Published var utcDateText: String = ""
    @Published var utcTimeText: String = ""
    @Published var kstDateText: String = ""
    @Published var kstTimeText: String = ""
    
    private var timerCancellable: AnyCancellable?
    private let utcDateFormatter: DateFormatter
    private let utcTimeFormatter: DateFormatter
    private let kstDateFormatter: DateFormatter
    private let kstTimeFormatter: DateFormatter
    
    init() {
        let locale = Locale(identifier: "ko_KR")
        
        let utcTZ = TimeZone(abbreviation: "UTC")!
        let kstTZ = TimeZone(identifier: "Asia/Seoul")!
        
        let dateFormat = "yyyy-MM-dd (EEE)"
        let timeFormat = "HH:mm:ss"
        
        let utcDate = DateFormatter()
        utcDate.locale = locale
        utcDate.timeZone = utcTZ
        utcDate.dateFormat = dateFormat
        utcDateFormatter = utcDate
        
        let utcTime = DateFormatter()
        utcTime.locale = locale
        utcTime.timeZone = utcTZ
        utcTime.dateFormat = timeFormat
        utcTimeFormatter = utcTime
        
        let kstDate = DateFormatter()
        kstDate.locale = locale
        kstDate.timeZone = kstTZ
        kstDate.dateFormat = dateFormat
        kstDateFormatter = kstDate
        
        let kstTime = DateFormatter()
        kstTime.locale = locale
        kstTime.timeZone = kstTZ
        kstTime.dateFormat = timeFormat
        kstTimeFormatter = kstTime
        
        updateNow()
        
        // 1초마다 갱신
        timerCancellable = Timer
            .publish(every: 1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                self?.updateNow()
            }
    }
    
    private func updateNow() {
        let now = Date()
        
        utcDateText = utcDateFormatter.string(from: now)
        utcTimeText = utcTimeFormatter.string(from: now)
        
        kstDateText = kstDateFormatter.string(from: now)
        kstTimeText = kstTimeFormatter.string(from: now)
    }
}


```



