package io.github.duzhaokun123.yabr.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import io.github.duzhaokun123.yabr.Main

/**
 * 这些东西应该在 [Main.main] 调用之前也能用
 */
object EarlyUtils {
    fun getProcessName(context: Context?): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Process.myProcessName()
        } else {
            if (context == null) {
                throw RuntimeException("Context is null, cannot get process name")
            }
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return activityManager.runningAppProcesses.firstOrNull { it.pid == Process.myPid() }?.processName
                ?: throw RuntimeException("Failed to get process name for PID ${Process.myPid()}")
        }
    }

    @SuppressLint("DiscouragedPrivateApi", "UnsafeDynamicallyLoadedCode")
    fun loadLibrary(name: String, context: Context? = null) {
        runCatching {
            System.loadLibrary(name)
        }.onFailure { t ->
//        AndroidLogger.w(t)
            val class_VMRuntime = Class.forName("dalvik.system.VMRuntime")
            val method_getRuntime = class_VMRuntime.getDeclaredMethod("getRuntime")
            method_getRuntime.isAccessible = true
            val method_vmInstructionSet = class_VMRuntime.getDeclaredMethod("vmInstructionSet")
            method_vmInstructionSet.isAccessible = true
            val arch = method_vmInstructionSet.invoke(method_getRuntime.invoke(null)) as String
            val lib = when (arch) {
                "arm" -> "armeabi-v7a"
                "arm64" -> "arm64-v8a"
                "x86" -> "x86"
                "x86_64" -> "x86_64"
                else -> throw RuntimeException("Unsupported architecture: $arch")
            }
            val modulePath =
                if (context != null) {
                    context.packageManager.getApplicationInfo(Main.packageName, 0).sourceDir
                } else {
                    loaderContext.modulePath
                }
            val path = "$modulePath!/lib/$lib/lib$name.so"
            System.load(path)
        }
    }
}
