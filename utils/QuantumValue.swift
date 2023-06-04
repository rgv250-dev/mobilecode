//코드 직렬화시 가끔 클래스로 받는 경우 값이 다른 경우가 있다 
//예시)dd_code가 불러오는 곳이 다른 경우 또는 Int, String인 경우 에러가 나는 경우가 있다 
//이 방식을 사용 시 받아오는 값이 달라도 문제 없이 사용 가능 
//선행 되어야 하는 것 (using: String.Encoding.utf8)! 사용 필수
//TestValue <-- 구문확인 하면 됨

struct GoodsListVo : Decodable {
    var cashMinPrc: String?
    var ovsMinPrcYn: String?
    var push_yn: String?
    var cashMinPrcYn: String?
    var modelno: String?
    var minPriceText : String?
    var reg_date : String?
    var graph_color: String?
    var modelnm: String?
    var minprice_regdate: String?
    var middleImageUrl: String?
    var mallcnt3: String?
    var ca_code: String?
    var p_pl_no: String?
    var adultImageFlag: String?
    
    var imgurl: String?
    var c_dateStr: String?
    var ca_lcode: String?
    var mallcnt: String?
    var c_date: String?
    var alarm_minprice: String?
    var folder_id: String?
    var pList_modelno: String?
    var price : String?
    var alarm_onoff: String?
    var factory: String?
    var smallImageUrl : String?
    var dd_name:String?
    var dd_code:TestValue?
    
    
    init?(_ data: [String: Any]) {
        cashMinPrc = data["cashMinPrc"] as? String? ?? ""
        ovsMinPrcYn = data["ovsMinPrcYn"] as? String? ?? ""
        push_yn = data["push_yn"] as? String? ?? ""
        cashMinPrcYn = data["cashMinPrcYn"] as? String? ?? ""
        modelno = data["modelno"] as? String? ?? ""
        minPriceText = data["minPriceText"] as? String? ?? ""
        reg_date = data["reg_date"] as? String? ?? ""
        graph_color = data["graph_color"] as? String? ?? ""
        modelnm = data["modelnm"] as? String? ?? ""
        minPriceText = data["minPriceText"] as? String? ?? ""
        reg_date = data["reg_date"] as? String? ?? ""
        graph_color = data["graph_color"] as? String? ?? ""
        modelnm = data["modelnm"] as? String? ?? ""
        minprice_regdate = data["minprice_regdate"] as? String? ?? ""
        middleImageUrl = data["middleImageUrl"] as? String? ?? ""
        mallcnt3 = data["mallcnt3"] as? String? ?? ""
        ca_code = data["ca_code"] as? String? ?? ""
        p_pl_no = data["p_pl_no"] as? String? ?? ""
        imgurl = data["imgurl"] as? String? ?? ""
        c_dateStr = data["c_dateStr"] as? String? ?? ""
        ca_lcode = data["ca_lcode"] as? String? ?? ""
        mallcnt = data["mallcnt"] as? String? ?? ""
        c_date = data["c_date"] as? String? ?? ""
        alarm_minprice = data["alarm_minprice"] as? String? ?? ""
        folder_id = data["folder_id"] as? String? ?? ""
        pList_modelno = data["pList_modelno"] as? String? ?? ""
        alarm_onoff = data["alarm_onoff"] as? String? ?? ""
        price = data["price"] as? String? ?? ""
        factory = data["factory"] as? String? ?? ""
        smallImageUrl = data["smallImageUrl"] as? String? ?? ""
        dd_name = data["dd_name"] as? String? ?? ""
        dd_code = data["dd_code"] as? TestValue
    }
    
}


enum TestValue: Decodable {

    case int(Int), string(String)

    init(from decoder: Decoder) throws {
        if let Int = try? decoder.singleValueContainer().decode(Int.self) {
            self = .int(Int)
            return
        }

        if let string = try? decoder.singleValueContainer().decode(String.self) {
            self = .string(string)
            return
        }

        throw TestValue.missingValue
    }

    enum TestValue:Error {
        case missingValue
    }

}
