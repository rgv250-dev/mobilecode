/**
 *  다수의 EditText를 바라보고 이후에 변경해야할 버튼을 바라보게 하는 이벤트
 * @param edList 적용할 다수의 Array<EditText
 * @param edButton 적용할 버튼
 * @param mContextData 현재 화면
 * @param colorData 컬러 코드
 * */
class ListTextWathcer(
    edList: Array<EditText>,
    edButton: TextView,
    mContextData: Context,
    colorData: Drawable
) : TextWatcher {
    var edButton: TextView
    var edList: Array<EditText>
    var mContext = mContextData
    var colorSet = colorData
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        for (editText in edList) {
            if (editText.text.toString().trim { it <= ' ' }.isEmpty()) {
                edButton.isEnabled = false
                edButton.background = mContext.getDrawable(R.Drawable.컬러네임) //백그라운드 상태에서 기본적으로 적용할 버튼 Drawable
                edButton.setTextColor(mContext.resources.getColor(R.color.gray_500, null)) //텍스트 값
                break
            } else {
                edButton.isEnabled = true
                edButton.background = colorSet
                edButton.setTextColor(mContext.resources.getColor(R.color.color_ffffff, null)) /
            }
        }
    }

    init {
        this.edButton = edButton
        this.edList = edList
    }
}