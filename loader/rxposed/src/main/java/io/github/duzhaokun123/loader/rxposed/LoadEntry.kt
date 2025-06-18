package io.github.duzhaokun123.loader.rxposed

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.os.Process
import android.util.Log
import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.duzhaokun123.hooker.pine.PineHookerContext
import io.github.duzhaokun123.loader.base.ImplementationInfo
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.utils.EarlyUtils

object LoadEntry {
    lateinit var application: Application
    lateinit var processName: String

    @Suppress("unused")
    @JvmStatic
    fun entry(context: Context, source: String, argument: String) {
        Log.d(
            "RxposedLoader",
            "LoadEntry: entry called with context=$context, source=$source, argument=$argument"
        )
        val hookerContext = PineHookerContext()
        val loaderContext = object : LoaderContext {
            override val implementationInfo: ImplementationInfo
                get() = ImplementationInfo(
                    name = "Rxposed",
                    version = "0.1.0",
                    description = "Rxposed Loader for Pine Hooker"
                )
            override val hostClassloader: ClassLoader
                get() = context.classLoader
            override val processName: String
                get() = this@LoadEntry.processName
            override val application: Application
                get() = this@LoadEntry.application
            override val modulePath: String
                get() = source

        }

        hookerContext.hookMethod(
            Instrumentation::class.java.getMethod(
                "callApplicationOnCreate", Application::class.java
            ),
            object : HookCallback {
                override fun before(callbackContext: HookCallbackContext) {
                    application = callbackContext.args[0] as Application
                    processName = EarlyUtils.getProcessName(application)
                    Main.main(loaderContext, hookerContext)
                }
            }
        )
    }
}