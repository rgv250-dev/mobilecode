//UILabel 유틸성 자료 

class UtilSpace :NSObject {
    
extension UILabel{
    
    func urlAllEncoding(_ url:String) -> String!{
        let allowedCharacterSet = (CharacterSet(charactersIn: "!*'();:@&=+$,/?%#[] ").inverted)
        return url.addingPercentEncoding(withAllowedCharacters: allowedCharacterSet) ?? ""
    }
    //취소선
    func cancelLine(){
        guard let t = text else {
            return
        }
        let attributeString =  NSMutableAttributedString(string: t)
        attributeString.addAttribute(NSAttributedString.Key.strikethroughStyle,
                                     value: NSUnderlineStyle.single.rawValue,
                                     range: NSMakeRange(0, attributeString.length))
        attributedText = attributeString
    }
    //하단선
    func underLine(){
        guard let t = text else {
            return
        }
        let attributedString = NSMutableAttributedString(string: t)
        attributedString.addAttribute(NSAttributedString.Key.underlineStyle,
                                      value: NSUnderlineStyle.single.rawValue,
                                      range: NSRange(location: 0, length: attributedString.length))
        attributedText = attributedString
    }

    func clearAttributed(){
        guard let t = text else {
            return
        }
        let attributedString = NSMutableAttributedString(string: t)
        attributedString.removeAttribute(NSAttributedString.Key.underlineStyle, range: NSRange(location: 0, length: attributedString.length))
        
        attributedString.removeAttribute(NSAttributedString.Key.strikethroughStyle, range: NSRange(location: 0, length: attributedString.length))
        
        attributedText = attributedString
    }
    
     func diffColor(source sourceText:String, change:String, changeColor:UIColor? = nil , changeFont : UIFont? = nil){
        
        let attribute = NSMutableAttributedString(string: sourceText)
        
        if changeColor != nil{
            
            attribute.addAttributes([
                .foregroundColor: changeColor!
            ], range: (attribute.string as NSString).range(of: change))
        }
       
        if changeFont != nil{
            attribute.addAttributes([
                .font: changeFont!
            ], range: (attribute.string as NSString).range(of: change))
        }
        
        
        
        self.attributedText = attribute
    }
    
}
}
