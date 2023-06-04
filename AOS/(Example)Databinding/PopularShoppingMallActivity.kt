import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.enuri.android.R
import com.enuri.android.adapter.PopularShoppingAdapter
import com.enuri.android.databinding.ActivityPopularShoppingMallBinding
import com.enuri.android.extend.activity.BaseActivity

import kotlinx.android.synthetic.main.activity_popular_shopping_mall.*
import kotlinx.android.synthetic.main.view_top_simple_header.*

/*인기 쇼핑몰 정렬순 
대이터 바인딩 예제 
 build.gradle에 추가
 dataBinding {
        enabled = true
 }
    */



class PopularShoppingMallActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityPopularShoppingMallBinding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_popular_shopping_mall
        )

        tv_title.text = "인기쇼핑몰"
        btn_back2.setOnClickListener { _ -> onBackPressed() }

        var inputData :  ArrayList<ShopData> = //데이터 받는 곳 보통은 retapi 통신 싱글톤 혹은 일반 데이터 받아오기용
        initSetShopData(inputData)




    }

    private fun initSetShopData(inputData: ArrayList<ShopData>) {
        val baseActivity: BaseActivity = this
        popular_shopping_recyclerView.layoutManager = GridLayoutManager(this, 4)
        val adapter = PopularShoppingAdapter(baseActivity, inputData)
        popular_shopping_recyclerView.adapter = adapter
        popular_shopping_recyclerView.addItemDecoration(SimpleDividerItemDecoration(2, 4))
    }

}
