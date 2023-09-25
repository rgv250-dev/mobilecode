BaseActivity에서 binding 쉽게 쓰는 법

```
/**
 * Created by rgv250-dev on 2020-04-14.
 * BaseActivity 
 * */

abstract class BaseActivity<B : ViewBinding>(
    val bindingFactory: (LayoutInflater) -> B
) : AppCompatActivity() {

    private var _binding: B? = null
    val binding get() = _binding!!

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}

예시
class LoginActivity : BaseActivity<ActivityLoginBinding>({ ActivityLoginBinding.inflate(it) }) {

binding.뷰이름 으로 사용




```

BaseFragment에서 쓰는 법

```

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

/**
 * Created by rgv250-dev on 2020-04-14.
 * BaseActivity 공용 BaseFragment 쓸 내용
 * */
abstract class BaseFragment<V : ViewBinding>(
    private val inflate: Inflate<V>
) : Fragment() {

    private lateinit var _binding: V
    val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }


}


```
