import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.enuri.android.R
import com.enuri.android.util.ShopLogoMap
import com.enuri.android.vo.LogoMainVo
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import org.json.JSONObject

/*기타 바인딩용 예시 
보통은 glide을 쓰는것을 추천 
ImageLoader는 걍 예시용 
Glide가 쓰기도 편하고 쉬움 */
@BindingAdapter("bind_image")
fun bindImage(view: ImageView, url: String?) {
    var imgSrc = url
    if (!imgSrc.isNullOrEmpty()) {
        ImageLoader.getInstance().displayImage(url,view,object: SimpleImageLoadingListener(){
            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                super.onLoadingFailed(imageUri, view, failReason)
            }
        })
    }else{
        view.setImageResource(R.drawable.whitebox3030)
    }
}



