주로 모든 EditText가 입력이 되어야지 다음 이벤트를 할 수 있도록 하려고 하는 곳에서 사용함

주로 회원 가입창에서 사용함

```
//사용할 곳에서 사용 방식
         val edList = arrayOf<EditText>(
            binding.뷰이름01,
            binding.뷰이름02,
            binding.뷰이름03,
            binding.뷰이름04
        )
        val textWatcher = ListTextWathcer(
            edList,
            binding.serialCheckBtn,
            topView,
            topView.resources.getDrawable(R.drawable.back_ground_ab_red_500_round_30, null)
        )
        for (editText in edList) editText.addTextChangedListener(textWatcher)

```

```
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
```