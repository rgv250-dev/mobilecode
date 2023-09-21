안드로이드 다이얼로그 (DialogFragment()) 사이즈 대응 법

대단한건 없고, DialogFragment()에 onStart

```
override fun onStart() {
    super.onStart()
    val dialog: Dialog? = dialog
    if (dialog != null) {
        if (mAct.resources.getInteger(R.integer.비교할 값) == 기준 사이즈) {
           Utils.dialogFragmentSetWidthPercent(적용할 사이즈)
        } else {
           Utils.dialogFragmentSetWidthPercent(또다른 적용할 사이즈)
        }
    }

}
```

공용으로 쓸 곳에 아래에 있는 코드 넣은 후 위에 코드 처럼 적용 하면 대부분 대응 된다.
나머지는 내부에 들어갈 디자인은 디자이너에게 부탁하자. 개발자들은 그 조금의 차이가 잘 안보인다



```
fun dialogFragmentSetWidthPercent(percentage: Int) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
}
```