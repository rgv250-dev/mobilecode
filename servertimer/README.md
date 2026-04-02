


변경점

앱 켰을때 시간 기준으로 갱신 하여 화면에 뿌려주는 코드 중요한건 아래 뷰 모델 끝


```
private val _utcTime = MutableStateFlow(ZonedDateTime.now(ZoneId.of("UTC"))) //
    val utcTime = _utcTime.asStateFlow()

    private val _kstTime = MutableStateFlow(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
    val kstTime = _kstTime.asStateFlow()

    private val eameplestTime = MutableStateFlow(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))


    init {
        viewModelScope.launch {
            while (true) {
                _utcTime.value = ZonedDateTime.now(ZoneId.of("UTC")) // 현재 시간 상태 UTC
                _kstTime.value = ZonedDateTime.now(ZoneId.of("Asia/Seoul")) // 현재 시간 상태 Asia/Seoul
                delay(1000) // 1초마다 갱신
            }
        }
    }

```



