내부 저장소에 토큰이나 다른 값을 저장하는 경우에 문제가 있다.

XML 파일이기에 외부에서 쉽게 파일을 읽을 수 있다.

또한 비동기 작업을 제대로 해주지 않으면 ANR을 발생시킬 수 있고,
런타임에 예외가 생기면 런타임 애러가 발생해 잘못 사용하면 앱이 강제 종료된다.
또한 strong consistency가 보장되는 api가 없어 다중 스레드 환경에서 다른 결과값이 생길 수 있다.

그럼에도 쓰는 이유는 뭐 여러가지가 있을것이다. 

사실 제일 좋은 방법은 Datastore 쓰는 것이긴 한대 이미 생성된 프로젝트를 바꿀수는 없기 때문에
중요한 내역은 아래의 방식을 선택 하여 업데이트라도 치는게 가장 좋을것이다 

업데이트 주기가 길고 여유가 된다면, Datastore로 갈아타자 

암호형 내부 저장소 코드는 이렇고 아래에 Datastore 추가 해두었다. 어느것이든 선택해서 쓰도록 하자
```

class SecuritySharedUtils(context: Context) {
    companion object {
        private const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"
        private const val KEY_SIZE = 256
        private const val PREFERENCE_FILE_KEY = "_preferences"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        val spec = KeyGenParameterSpec
            .Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .build()

        val masterKey = MasterKey
            .Builder(context)
            .setKeyGenParameterSpec(spec)
            .build()

        EncryptedSharedPreferences.create(
            context,
            context.packageName + PREFERENCE_FILE_KEY,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun putAny(key: String, value: Any) {
        val editor = sharedPreferences.edit()

        with(editor) {
            when (value) {
                is Int -> {
                    putInt(key, value)
                }

                is String -> {
                    putString(key, value)
                }

                is Boolean -> {
                    putBoolean(key, value)
                }

                else -> throw IllegalArgumentException("잘못된 인자입니다.")
            }
            apply()
        }
    }

    fun getString(key: String) = sharedPreferences.getString(key, "")

    fun getNumber(key: String) = sharedPreferences.getInt(key, 0)

    fun getBoolean(key: String) = sharedPreferences.getBoolean(key, false)
}

```

그리고 이코드는 DataStore 방식이다. 원하는대로 쓰자

```

private val Context.dataStore by preferencesDataStore(name = "secure_data_store")

object DataStoreHelper {
    fun saveEncryptedData(context: Context, key: String, value: String) {
        val dataStoreKey = stringPreferencesKey(key)
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[dataStoreKey] = value
            }
        }
    }

    fun getEncryptedData(context: Context, key: String): String? {
        val dataStoreKey = stringPreferencesKey(key)
        return runBlocking {
            context.dataStore.data.first()[dataStoreKey]
        }
    }
}
```


```

object KeyStoreUtil {

    private const val KEY_ALIAS = "init_secure_key"

    private const val CIPHER_ALGORITHM =
        "${KeyProperties.KEY_ALGORITHM_RSA}/" +
                "${KeyProperties.BLOCK_MODE_ECB}/" +
                KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1

    private lateinit var keyEntry: KeyStore.Entry
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKeyPair()
        }
        keyEntry = keyStore.getEntry(KEY_ALIAS, null)
        isInitialized = true
    }

    private fun generateKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            "AndroidKeyStore"
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()
        keyPairGenerator.initialize(keyGenParameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    fun saveEncryptedData(context: Context, key: String, plainText: String) {
        val encryptedData = encrypt(plainText)
        DataStoreHelper.saveEncryptedData(context, key, encryptedData)
    }

    fun getDecryptedData(context: Context, key: String): String? {
        val encryptedData = DataStoreHelper.getEncryptedData(context, key)
        return encryptedData?.let { decrypt(it) }
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM).apply {
            init(Cipher.ENCRYPT_MODE, (keyEntry as KeyStore.PrivateKeyEntry).certificate.publicKey)
        }
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decrypt(encryptedData: String): String {
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, (keyEntry as KeyStore.PrivateKeyEntry).privateKey)
        }
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}


```



```

// 초기화
KeyStoreUtil.init(context)

// 데이터 저장
KeyStoreUtil.saveEncryptedData(context, "secure_key", "This is a secret!")

// 데이터 읽기
val decryptedData = KeyStoreUtil.getDecryptedData(context, "secure_key")
Log.d("DecryptedData", decryptedData ?: "No data found")


```
