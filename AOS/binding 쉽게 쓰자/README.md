BaseActivity에서 binding 쉽게 쓰기

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

