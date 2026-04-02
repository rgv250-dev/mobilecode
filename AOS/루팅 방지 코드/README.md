루팅 확인 및 디버그등 못하게 하는 코드
이거랑 적어도 기본적인 난독화만 해도 반절은 먹고 들어감
제발 디버그는 따로 뽑아다 써..

```
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.partyvelope.puddingspoon.core.util.AppDebugLogger
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

object RootCheckMessenger {

    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun isInstalledFromPlayStore(context: Context): Boolean {
        val installer = try {
            context.packageManager
                .getInstallSourceInfo(context.packageName)
                .installingPackageName
        } catch (e: Exception) {
            null
        }
        return installer == "com.android.vending"
    }

    private fun checkRootFiles(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/system/bin/.ext/.su",
            "/system/xbin/mu",
            "/system/sd/xbin/su",
            "/system/usr/we-need-root",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/su",
            "/system/bin/failsafe/su",
            "/system/xbin/busybox",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            reader.close()
            process.destroy()
            line != null
        } catch (_: Exception) {
            false
        }
    }

    private fun checkBuildTags(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }

    private fun checkRootProperties(): Boolean {
        val keys = listOf("ro.build.fingerprint", "ro.debuggable", "ro.secure")
        return keys.any { key ->
            val property = getSystemProperty(key)
            property != null && (property.contains("test-keys") || property.contains("eng"))
        }
    }

    private fun getSystemProperty(propName: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            process.destroy()
            result
        } catch (_: Exception) {
            null
        }
    }

    private fun checkRootApps(context: Context): Boolean {
        val rootApps = arrayOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su"
        )
        val pm: PackageManager = context.packageManager
        return rootApps.any { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun isDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected()
    }

    private fun detectFridaInMaps(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists()) return false
            mapsFile.bufferedReader().useLines { lines ->
                lines.any { it.contains("frida", ignoreCase = true) }
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isDeviceRooted(context: Context): Boolean {
        return checkRootFiles() ||
                checkSuCommand() ||
                checkBuildTags() ||
                checkRootProperties() ||
                checkRootApps(context) ||
                isDebuggerConnected() ||
                detectFridaInMaps()
    }

    fun blockIfInvalidInstaller(context: Context) {
        val debug = isDebuggable(context)

        if (!debug) {
            // 릴리즈: Play Store 설치 아니면 종료
            if (!isInstalledFromPlayStore(context)) {
                exitProcess(0)
            }
            // 릴리즈: 루팅 감지면 종료
            if (isDeviceRooted(context)) {
                exitProcess(0)
            }
        } else {
            // 디버그: 종료하지 않고 로그만
            if (isDeviceRooted(context)) {
                AppDebugLogger.d("RootCheckMessenger","DEBUG: 루팅된 기기 감지됨 - 앱 종료하지 않음",)
            }
        }
    }
}

```

