package io.github.duzhaokun123.loader.inline

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import io.github.duzhaokun123.hooker.noop.NoOpHookerContext
import io.github.duzhaokun123.hooker.pine.PineHookerContext
import io.github.duzhaokun123.loader.base.ImplementationInfo
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.utils.EarlyUtils
import io.github.duzhaokun123.yabr.utils.runNewThread

@SuppressLint("PrivateApi")
object InlineEntryKt {
    const val TAG = "InlineEntry"

    lateinit var stackTrace: String
    lateinit var hooker: String

    @JvmStatic
    fun entry(hooker: String) {
        stackTrace = Throwable().stackTraceToString()
        this.hooker = hooker

        Log.d(TAG, "entry: $hooker")

        if (InlineEntry.application != null) {
            onApplicationReady(InlineEntry.application)
        } else {
            val class_ActivityThread =
                Class.forName("android.app.ActivityThread")
            val method_ActivityThread_currentApplication =
                class_ActivityThread.getMethod("currentApplication")
            runNewThread {
                while (true) {
                    val application =
                        method_ActivityThread_currentApplication.invoke(null) as Application?
                    if (application == null) {
                        Thread.sleep(200)
                    } else {
                        onApplicationReady(application)
                        break
                    }
                }
            }
        }
    }

    fun onApplicationReady(application: Application) {
        val hookerContext = when (hooker) {
            "pine" -> PineHookerContext(application)
            "noop" -> NoOpHookerContext
            else -> throw RuntimeException("Unsupported hooker: $hooker")
        }

        val processName = EarlyUtils.getProcessName(application)
        val pm = application.packageManager
        val moduleInfo = pm.getApplicationInfo(Main.packageName, 0)
        val modulePath = moduleInfo.sourceDir

        val loaderContext = object : LoaderContext {
            override val implementationInfo: ImplementationInfo
                get() = ImplementationInfo(
                    name = "InlineLoader",
                    version = "0.1.0",
                    description = "previous stage loader: ${InlineEntry.previousStageLoader}\n" +
                            "stackTrace:\n$stackTrace"
                )
            override val hostClassloader: ClassLoader
                get() = application.classLoader
            override val processName: String
                get() = processName
            override val application: Application
                get() = application
            override val modulePath: String
                get() = modulePath
        }
        Main.main(loaderContext, hookerContext)
    }
}