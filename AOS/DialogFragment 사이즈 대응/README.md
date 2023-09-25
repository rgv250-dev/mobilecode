안드로이드 다이얼로그 (DialogFragment()) 사이즈 대응 법

DialogFragment()에 onStart

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

공용으로 쓰는 곳이니 따로 유틸로 관리 하는 곳에 추가하여

```
 fun setWidthPercent(percentage: Int, dialog: Dialog) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
```
