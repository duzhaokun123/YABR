package io.github.duzhaokun123.loader.acf

import android.annotation.SuppressLint
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.duzhaokun123.hooker.pine.PineHookerContext
import io.github.duzhaokun123.loader.base.ImplementationInfo
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.utils.EarlyUtils
import io.github.duzhaokun123.yabr.utils.getFieldValue
import io.github.duzhaokun123.yabr.utils.invokeStaticMethod

@SuppressLint("PrivateApi")
@RequiresApi(Build.VERSION_CODES.P)
object Loader {
    lateinit var activityThread: Any
    lateinit var loadedApk: Any
    lateinit var application: Application

    @JvmStatic
    fun load() {
        val class_ActivityThread =
            Class.forName("android.app.ActivityThread")
        activityThread = class_ActivityThread.getMethod("currentActivityThread").invoke(null)!!
        val context = createLoadedApkWithContext()
        val pm = context.packageManager
        val applicationInfo = pm.getApplicationInfo(context.packageName, 0)

        val yabrPath = pm.getApplicationInfo(Main.packageName, 0).sourceDir

        val hookerContext = PineHookerContext(application)
        val processName = EarlyUtils.getProcessName(context)
        val loaderContext = object : LoaderContext {
            override val implementationInfo: ImplementationInfo
                get() = ImplementationInfo(
                    name = "AppComponentFactory",
                    version = "0.1.0",
                    description = "replaced AppComponentFactory ${AppComponentFactory::class}",
                )
            override val hostClassloader: ClassLoader
                get() = this@Loader.javaClass.classLoader!!
            override val processName: String
                get() = processName
            override val application: Application
                get() = this@Loader.application
            override val modulePath: String
                get() = yabrPath
        }

        hookerContext.hookMethod(
            Instrumentation::class.java.getMethod("callApplicationOnCreate", Application::class.java),
            object : HookCallback {
                override fun before(callbackContext: HookCallbackContext) {
                    application = callbackContext.args[0] as Application
                    Main.main(loaderContext, hookerContext)
                }
            }
        )

        val originalAppComponentFactory = runCatching {
            applicationInfo.metaData.getString("originalAppComponentFactory")
        }.getOrNull()
        if (originalAppComponentFactory != null) {
            try {
                Class.forName(originalAppComponentFactory)
            } catch (e: ClassNotFoundException) {
                throw RuntimeException(
                    "Failed to load original app component factory: $originalAppComponentFactory",
                    e
                )
            }
        }
    }

    fun createLoadedApkWithContext(): Context {
        try {
            val mBoundApplication = activityThread.getFieldValue("mBoundApplication")!!
            loadedApk = mBoundApplication.getFieldValue("info")!!
            return Class.forName("android.app.ContextImpl")
                .invokeStaticMethod("createAppContext", activityThread, loadedApk) as Context
        } catch (e: Exception) {
            throw RuntimeException("Failed to create LoadedApk with Context", e)
        }
    }
}