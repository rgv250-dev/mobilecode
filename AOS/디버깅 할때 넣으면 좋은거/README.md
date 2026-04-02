매 깃허브 만들면 뭐 하나, 이력서에 주소 넣어도 읽지도 않는데

```
import android.util.Log

object AppDebugLogger {
    private const val TAG = "puddingspoon"

    @Volatile private var enabled: Boolean = false

    fun init(isDebug: Boolean) {
        enabled = isDebug
    }

    private inline fun logIfDebug(action: () -> Unit) {
        if (enabled) action()
    }

    fun d(msg: String, string: String) = logIfDebug { Log.d(TAG, msg) }
    fun e(msg: String, tr: Throwable? = null) = logIfDebug { Log.e(TAG, msg, tr) }
}

```

