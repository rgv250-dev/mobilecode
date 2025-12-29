https://support.google.com/faqs/answer/9450925?hl=en


변경점

AES/CBC → AES/GCM 변경 
무결성(변조 감지)

현대 표준(AEAD)GCM이 기본값 (재사용 안됨)

예전 구조(메모리 var token)
멀티 스레드/멀티 요청에서 값 꼬이기 쉬웠던것 수정

```
data class RefreshRequest(val refreshToken: String)
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

interface ApiService {
    @POST("refreshToken")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse
}

```


```
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val access = runBlocking { tokenStore.accessTokenFlow.first() }

        val req = chain.request().newBuilder().apply {
            if (!access.isNullOrBlank()) {
                header("Authorization", "Bearer $access")
            }
        }.build()

        return chain.proceed(req)
    }
}

```

```
data class EncryptedPayload(
    val iv: ByteArray,
    val cipherText: ByteArray
) {
    fun toBase64String(): String {
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val ctB64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
        return "$ivB64:$ctB64"
    }

    companion object {
        fun fromBase64String(value: String): EncryptedPayload {
            val parts = value.split(":")
            require(parts.size == 2) { "Invalid payload format" }
            return EncryptedPayload(
                iv = Base64.decode(parts[0], Base64.NO_WRAP),
                cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
            )
        }
    }
}

class KeystoreManager(
    private val keyAlias: String = "token_key_alias",
) {
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    init {
        generateKeyIfNeeded()
    }

    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getKey(): SecretKey {
        // 키가 지워졌거나 깨진 경우 대비
        val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        if (entry == null) {
            generateKeyIfNeeded()
            return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
        }
        return entry.secretKey
    }

    fun encrypt(plainText: String): EncryptedPayload {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return EncryptedPayload(iv = cipher.iv, cipherText = cipherText)
    }

    fun decrypt(payload: EncryptedPayload): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, payload.iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        val decoded = cipher.doFinal(payload.cipherText)
        return String(decoded, Charsets.UTF_8)
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
    }
}
```

```
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 보통은 BuildConfig로 빼서 flavor로 관리 추천
    @Provides
    @Singleton
    fun provideBaseUrl(): String = "https://api.example.com/"

    @Provides
    @Singleton
    fun provideConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

    /**
     * 무인증 OkHttp: refresh 전용 (Authenticator/Interceptor 없음)
     */
    @Provides
    @Singleton
    @Named("noAuthOkHttp")
    fun provideNoAuthOkHttp(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    @Named("noAuthRetrofit")
    fun provideNoAuthRetrofit(
        baseUrl: String,
        @Named("noAuthOkHttp") client: OkHttpClient,
        gson: GsonConverterFactory
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(gson)
        .build()

    @Provides
    @Singleton
    @Named("noAuthAuthApi")
    fun provideNoAuthAuthApi(@Named("noAuthRetrofit") retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    /**
     * 인증 OkHttp: 토큰 헤더 + 401 refresh
     */
    @Provides
    @Singleton
    fun provideAuthOkHttp(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        baseUrl: String,
        client: OkHttpClient,
        gson: GsonConverterFactory
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(gson)
        .build()

    // 앱에서 쓰는 API들은 이 retrofit에서 create 해서 주입하면 됨
}
```

```
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val authApiNoAuth: ApiService,
    private val sessionEvents: SessionEvents,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // refreshToken 호출 자체가 401이면 루프 방지
        if (response.request.url.encodedPath.contains("refreshToken")) return null

        if (responseCount(response) >= 2) {
            sessionEvents.onForceLogout()
            return null
        }

        val newAccess: String? = runBlocking {
            mutex.withLock {
                val currentAccess = tokenStore.accessTokenFlow.first()
                val requestAccess = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")
                    ?.trim()

                // 이미 다른 요청이 갱신해둔 토큰이면 그걸 사용
                if (!currentAccess.isNullOrBlank() && currentAccess != requestAccess) {
                    return@withLock currentAccess
                }

                val refresh = tokenStore.refreshTokenFlow.first() ?: return@withLock null

                runCatching {
                    val refreshed = authApiNoAuth.refresh(RefreshRequest(refresh))
                    tokenStore.saveTokens(refreshed.accessToken, refreshed.refreshToken)
                    refreshed.accessToken
                }.getOrNull()
            }
        }

        if (newAccess.isNullOrBlank()) {
            sessionEvents.onForceLogout()
            return null
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

interface SessionEvents {
    fun onForceLogout()
}
```

```
private val Context.tokenDataStore by preferencesDataStore(name = "token_store")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crypto: KeystoreManager,
) {
    private val KEY_ACCESS = stringPreferencesKey("access_token_enc")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token_enc")

    val accessTokenFlow: Flow<String?> = context.tokenDataStore.data.map { prefs ->
        prefs[KEY_ACCESS]?.let { decryptOrNull(it) }
    }

    val refreshTokenFlow: Flow<String?> = context.tokenDataStore.data.map { prefs ->
        prefs[KEY_REFRESH]?.let { decryptOrNull(it) }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[KEY_ACCESS] = crypto.encrypt(accessToken).toBase64String()
            prefs[KEY_REFRESH] = crypto.encrypt(refreshToken).toBase64String()
        }
    }

    suspend fun clear() {
        context.tokenDataStore.edit { it.clear() }
    }

    private fun decryptOrNull(enc: String): String? = runCatching {
        crypto.decrypt(EncryptedPayload.fromBase64String(enc))
    }.getOrNull()
}
```
