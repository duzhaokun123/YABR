package io.github.duzhaokun123.yabr.zygisk

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import dalvik.system.DexClassLoader

@SuppressLint("PrivateApi")
@Suppress("unused")
object Loader {
    const val TAG = "YABR_zygisk_loader"

    lateinit var modulePackageName: String
    var hookerName: String? = null

    @JvmStatic
    fun load(arg0: String?, arg1: String?) {
        Log.d(TAG, "load: $arg0, $arg1")
        if (arg0 == null) {
            Log.e(TAG, "can't load no module package name")
            return
        }

        modulePackageName = arg0
        hookerName = arg1

        val class_ActivityThread =
            Class.forName("android.app.ActivityThread")
        val method_ActivityThread_currentApplication =
            class_ActivityThread.getMethod("currentApplication")
        Thread {
            var application: Application?
            while (true) {
                application =
                    method_ActivityThread_currentApplication.invoke(null) as Application?
                if (application == null) {
                    Thread.sleep(200)
                } else {
                    break
                }
            }
            onApplicationReady(application)
        }.start()
    }

    fun onApplicationReady(application: Application) {
        val pm = application.packageManager
        val moduleInfo = pm.getApplicationInfo(modulePackageName, 0)
        val modulePath = moduleInfo.sourceDir
        Log.d(TAG, "load module $modulePath")
        val moduleClassloader =
            DexClassLoader(modulePath, application.cacheDir.path, null, null)
        runCatching {
            val entry = moduleClassloader.loadClass("io.github.duzhaokun123.loader.inline.InlineEntry")
            if (hookerName == null) {
                entry.getMethod("entry0").invoke(null)
            } else {
                entry.getMethod("entry1").invoke(null, hookerName)
            }
        }.onFailure { t ->
            Log.e(TAG, "load module fail", t)
        }
    }
}