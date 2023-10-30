

/*
  ios 라디오 버튼 형태의 다이얼로그 

*/


if self.superMarketFilterAlertUIView == nil{
                  self.superMarketFilterAlertUIView = SuperMarketFilterAlertUIView()
                  self.superMarketFilterAlertUIView!.translatesAutoresizingMaskIntoConstraints = false
                  self.view.addSubview(self.superMarketFilterAlertUIView!)
                  NSLayoutConstraint.activate([
                      self.superMarketFilterAlertUIView!.leadingAnchor.constraint(equalTo: self.view.leadingAnchor),
                      self.superMarketFilterAlertUIView!.topAnchor.constraint(equalTo:self.view.topAnchor),
                      self.superMarketFilterAlertUIView!.bottomAnchor.constraint(equalTo:self.view.bottomAnchor),
                      self.superMarketFilterAlertUIView!.trailingAnchor.constraint(equalTo: self.view.trailingAnchor)
                  ])
                  self.superMarketFilterAlertUIView!.isHidden = true
                  self.superMarketFilterAlertUIView!.delegateFilter = self
              }
        
        self.superMarketFilterAlertUIView?.isHidden = false 






class SuperMarketFilterAlertUIView: UIView, UITableViewDataSource, UITableViewDelegate {


    @IBOutlet weak var tableVeiw: UITableView!
    @IBOutlet weak var contentView: UIView!
    @IBOutlet weak var button: UIButton!
    
    var testDataList : [SuperMarketFillterDataVo] = [
           SuperMarketFillterDataVo(name: "전체", selected: false, code: "0"),
           SuperMarketFillterDataVo(name: "이마트몰", selected: false, code: "0"),
           SuperMarketFillterDataVo(name: "쿠팡", selected: false, code: "0"),
           SuperMarketFillterDataVo(name: "홈플러스", selected: false, code: "0"),
           SuperMarketFillterDataVo(name: "롯데마트몰", selected: false, code: "0")]
    
    var delegateFilter : SuperMarketFilterAlartProtocol?
    
    var passDataIndex : Int = 0
    
    private let radioButton = "SuperMarketFilterCell"
    
    override init(frame: CGRect) {
           super.init(frame: frame)
           commonInit()
    }
       
    required init?(coder aDecoder: NSCoder) {
           super.init(coder: aDecoder)
           commonInit()
    }
       
    func commonInit() {
           Bundle.main.loadNibNamed("SuperMarketFilterAlertUIView", owner: self, options: nil)
        
            contentView.fixInView(self)
        
            let lineFrame = UIView()
            lineFrame.translatesAutoresizingMaskIntoConstraints = false
           
            tableVeiw.dataSource = self
            tableVeiw.delegate = self
            tableVeiw.tableFooterView = UIView()
            tableVeiw.register(UINib(nibName: radioButton, bundle: nil), forCellReuseIdentifier: radioButton)
            tableVeiw.separatorStyle = .none
               
            updateSelectedIndex(0)
        
       }
    

    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
           return testDataList.count
       }
       
       func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
           
           guard let cell = tableView.dequeueReusableCell(withIdentifier: radioButton, for: indexPath) as? SuperMarketFilterCell else { fatalError("Cell Not Found") }
           cell.selectionStyle = .none
           
           let data = testDataList[indexPath.row]
           let currentIndex = indexPath.row
           let selected = currentIndex == selectedFilter
           cell.configure(data.name)
           cell.isSelected(selected)
           
           
           
           return cell
       }
       
       func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
           updateSelectedIndex(indexPath.row)
       }
       

      private var selectedFilter: Int? {
           didSet {
               self.tableVeiw.reloadData()
           }
       }
       
       private func updateSelectedIndex(_ index: Int) {
           selectedFilter = index
           passDataIndex = index
       }
    
    
    @IBAction func endButtonEvent(_ sender: Any) {
           var superMarketFillterDataVo : SuperMarketFillterDataVo = testDataList[passDataIndex]
           self.delegateFilter?.SuperMarketFilterSeleter(_data: superMarketFillterDataVo)
          
       }
       
}
