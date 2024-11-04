필요한 곳에서 불러서 쓰기 위해서 이렇게 구성하였다.
원래는 생체인증 마다 여러가 방법은 있지만 일단 토큰 받는 기능이 따로 있기 때문에 거기에 맞추어 작업하였다.
```

object BioAuthManger {

    private lateinit var executor: Executor
    lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    lateinit var mListener: AuthListener

    val bioAuthTag = "bioAuthTag"

    interface AuthListener {
        fun onAuthError()
        fun onAuthSuccess()
        fun onAuthFailed()
    }

    fun canAuth(context: Context): Int {
        // BIOMETRIC_STRONG 은 안드로이드 11 에서 정의한 클래스 3 생체 인식을 사용하는 인증 - 암호화된 키 필요
        // BIOMETRIC_WEAK 은 안드로이드 11 에서 정의한 클래스 2 생체 인식을 사용하는 인증 - 암호화된 키까지는 불필요
        // DEVICE_CREDENTIAL 은 화면 잠금 사용자 인증 정보를 사용하는 인증 - 사용자의 PIN, 패턴 또는 비밀번호
        val biometricManager = BiometricManager.from(context)
        val responseCode = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
        when (responseCode) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(bioAuthTag, "App can authenticate using biometrics.")
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e(bioAuthTag, "No biometric features available on this device.")
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e(bioAuthTag, "Biometric features are currently unavailable.")
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e(bioAuthTag, "지문이 등록 되지 않음.")
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->{
                Log.e(bioAuthTag, "기기가 현재 생체 인식을 지원하지 않음")
            }
        }
        return responseCode
    }

    fun initialize(context: Context, activity: Activity) {
        if (canAuth(context) != 0) {
            Log.e(bioAuthTag, "initialize failed!! code : ${canAuth(context)}")
            return
        }

        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(activity as FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    mListener.onAuthError()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    mListener.onAuthSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.e(bioAuthTag, "onAuthenticationFailed?")
                    mListener.onAuthFailed()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("생체 인증 로그인")
            .setAllowedAuthenticators(BIOMETRIC_WEAK)
            .setDescription("프리모 차주앱 로그인 진행을 위해 인증을 해주세요.")
            .setNegativeButtonText("취소")
            .build()

    }

    fun floatingAuth(mListener: AuthListener) {
        biometricPrompt.authenticate(promptInfo)
        BioAuthManger.mListener = mListener
    }

}


```
메소드 호출

```

   /**
     * bioAuth
     * 생체 인증용 메소드
     */
    fun bioAuth() {

        val canAuthCode: Int = BioAuthManger.canAuth(this@MainActivity) //생체 인증이 가능한지 확인 

        if (canAuthCode == 0) {
            runOnUiThread {
                BioAuthManger.initialize(this, this@MainActivity)
                BioAuthManger.floatingAuth(object : BioAuthManger.AuthListener {
                    override fun onAuthError() {
                        binding.mainWebView.post {
                            binding.mainWebView.loadUrl("javascript:closeRegisterEasyLogin()")
                        }
                    }

                    override fun onAuthSuccess() {

                       
                            val token =
                                SecuritySharedUtils(this@MainActivity).getString(불러올 키값 이름) //암호 저장소에서 가져오도록하자 토큰은 내부저장소 같은에 넣으면 안된다.

                            if (token?.isNotEmpty() == true) {
                                //앱 토큰 발급하러 가기
                            }

                             //API 호출 후 Refresh Token만료 되어서 재발급 시키거나 다음 프로세스로 이동하면 된다.

                    }

                    override fun onAuthFailed() {
                        Log.d("incaseTest", "aaaaaadfadf")
                    }
                })
            }
        } else if (canAuthCode == 11) {
            makeToast("생체 인증을 먼저 등록 한 후에 사용 가능 합니다.")
        } else {
            makeToast("생체 인증을 지원 하지 않는 기기 입니다.")
        }
    }


```
