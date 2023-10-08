대충 통신 하는 법


```

class NoteMoreRepository(private val apiService: testAPI) {
    val data = SingleLiveEvent<Data>()
    var fetch = SingleLiveEvent<String>()

    fun loadData(token: String, booidx: Int) {

        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.e("NoteMore initData", "throwable : " + throwable.localizedMessage)

            when (throwable) {
                is SocketException -> fetch.postValue(throwable.toString()) //디버그용 스트링 값
                is HttpException -> fetch.postValue(throwable.toString())
                is UnknownHostException -> fetch.postValue(throwable.toString())
                else -> fetch.postValue(throwable.toString())
            }

        }

        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val getDataAsync = apiService.getWrongAnswerNoteMore(
                token,
                booidx
            )

            if (getDataAsync.isSuccessful && getDataAsync.body()?.success == true) {
                data.postValue(getDataAsync.body()?.data)
            }
        }

    }


}

```

```
@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun provideNoteMoreRepository(apiService: testAPI): NoteMoreRepository {
        return NoteMoreRepository(apiService) //노트 더보기 페이지 구성용
    }


    @Provides
    @Singleton
    fun provideApiService(): AnswerBookAPI {
        return AnswerBookRestUtil.api
    }


}
```

```

@HiltViewModel
class NoteMoreViewModel @Inject constructor(private val repo: NoteMoreRepository) : ViewModel() {
    val moreViewData = repo.data
    val fetch = repo.fetch

    fun loadData(token: String, bookIdx: Int) {
        repo.loadData(token, bookIdx)
    }
}

```

```
 override fun onCreate(savedInstanceState: Bundle?) {


  viewModel = ViewModelProvider(this@NoteMore)[NoteMoreViewModel::class.java] //뷰모델 추가

  viewModel.loadData(
                token,
                bookIdx
            )


viewModel.moreViewData.observe(this@NoteMore, Observer {
                it.let {
                    //데이터 용도
            })

            //에러 메세징용
            viewModel.fetch.observe(this@NoteMore) {
                //디버그 용 내용 토스트 메세지나 다이얼로그 호출 
            }


```
