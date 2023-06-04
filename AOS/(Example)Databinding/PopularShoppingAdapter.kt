import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.enuri.android.R
import com.enuri.android.binding.ShopClickBinding
import com.enuri.android.databinding.CellPopularShoppingmallBinding
import com.enuri.android.extend.activity.BaseActivity


class PopularShoppingAdapter (private val context: BaseActivity, private val shopData : ArrayList<ShopData>) :
        RecyclerView.Adapter<PopularShoppingAdapter.ViewAgency>() {

    //셀 세팅
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewAgency {
        val binding: CellPopularShoppingmallBinding = DataBindingUtil.inflate(LayoutInflater.from(context)
                , R.layout.cell_popular_shoppingmall, parent, false)
        return ViewAgency(binding)
    }

    override fun onBindViewHolder(holder: PopularShoppingAdapter.ViewAgency, position: Int) {
        val movieListModel = shopData[position]
        //데이터 바인딩 일반
        holder.mBinding.mallData = movieListModel
        //클릭 이벤트 바딩딩용
        holder.mBinding.handlers = ShopClickBinding(context, holder.mBinding.cellMain, shopData[position])
        holder.mBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return shopData.size
    }

    class ViewAgency (val mBinding: CellPopularShoppingmallBinding)
        : RecyclerView.ViewHolder(mBinding.root)


}
