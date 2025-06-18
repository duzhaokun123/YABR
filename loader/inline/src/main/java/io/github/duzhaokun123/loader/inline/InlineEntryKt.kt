package io.github.duzhaokun123.loader.inline

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.Process
import io.github.duzhaokun123.hooker.pine.PineHookerContext
import io.github.duzhaokun123.loader.base.ImplementationInfo
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.utils.EarlyUtils

object InlineEntryKt {
    @SuppressLint("PrivateApi")
    @JvmStatic
    fun entry(modulePath: String, hooker: String) {
        val hookerContext = when (hooker) {
            "pine" -> PineHookerContext()
            else -> throw RuntimeException("Unsupported hooker: $hooker")
        }
        val class_ActivityThread =
            Class.forName("android.app.ActivityThread")
        val activityThread = class_ActivityThread.getMethod("currentActivityThread").invoke(null)
        val application =
            class_ActivityThread.getMethod("getApplication").invoke(activityThread) as Application
        val processName = EarlyUtils.getProcessName(application)
        val trace =
            runCatching { throw RuntimeException() }.exceptionOrNull()!!.stackTraceToString()
        val loaderContext = object : LoaderContext {
            override val implementationInfo: ImplementationInfo
                get() = ImplementationInfo(
                    name = "InlineLoader",
                    version = "0.1.0",
                    description = "call trace:\n$trace"
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