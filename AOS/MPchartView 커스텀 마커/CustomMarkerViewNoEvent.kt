//Mpchart 라이브러리에서 앤써북 교재별 학습현황에 아이템 내부에서 확인 가능 
//커스텀 마커가 화면에서 벗어나지 않고 나오도록 하는 코드
class CustomMarkerViewNoEvent// mpchart view marker
    (
    context: Context?,
    layoutResource: Int,
    lineChartData: LineChart,
    var topView: ReportGroupRoundHolder
) :
    MarkerView(context, layoutResource) {

    var tooltip01: TextView //각각에 뷰에서 사용할 뷰 적용 
    var tooltip02: TextView
    var lineChart: LineChart //사용 되는 라인 차트
    private val uiScreenWidth = resources.displayMetrics.widthPixels
    private val uiScreenHeight = resources.displayMetrics.heightPixels

    init {
        tooltip01 = findViewById(R.id.tooltip01)
        tooltip02 = findViewById(R.id.tooltip02)
        lineChart = lineChartData
    }
    
    override fun draw(canvas: Canvas?, posX: Float, posY: Float) { //뷰를 그리는 곳 
        var newPosX = posX
        var newPosY = posY
        if (uiScreenWidth - posX < width) {
            newPosX -= width
        } else {
            newPosX -= (width / 2).toFloat()
        }
        newPosY -= if (uiScreenHeight - posY < height) {
            height
        } else {
            height
        }

        super.draw(canvas, newPosX, newPosY)
    }


    // entry를 content의 텍스트에 지정
    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        try {

            var data01 = e?.let {
                lineChart.data.getDataSetByIndex(1)
                    .getEntryForXValue(it.x, Float.NaN, DataSet.Rounding.CLOSEST)
            } as Entry

            var data02 = e.let {
                lineChart.data.getDataSetByIndex(0)
                    .getEntryForXValue(e.x, Float.NaN, DataSet.Rounding.CLOSEST)
            } as Entry 


            tool_tip_10_title.text = "" + data01.y.toInt()
            tool_tip_title.text = "" + data02.y.toInt()

        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e("CustomMarkerViewNoEvent", "ex : $ex")
        }
        super.refreshContent(e, highlight)
    }

}