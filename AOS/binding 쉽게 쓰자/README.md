BaseActivity에서 binding 쉽게 쓰는 법

```
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by Dev.Son on 2023-12-13.
 * BaseActivity 공용 액티비티에서 공용으로 쓸 것 들 모음
 */
abstract class BaseActivity<B : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B
) : AppCompatActivity() {

    private var _binding: B? = null
    val binding: B
        get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreate and onDestroy")

    private val mFirebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preLoad()
        _binding = bindingFactory(layoutInflater).also { setContentView(it.root) }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    abstract fun preLoad()
}


예시
class LoginActivity : BaseActivity<ActivityLoginBinding>({ ActivityLoginBinding.inflate(it) }) {

binding.뷰이름 으로 사용




```

BaseFragment에서 쓰는 법

```

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<V : ViewBinding>(
    private val inflate: Inflate<V>
) : Fragment() {

    private var _binding: V? = null
    val binding: V
        get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


```
