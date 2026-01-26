package io.github.duzhaokun123.yabr.module.core

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Looper
import io.github.duzhaokun123.codegen.ModuleActivities
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.R as AppR
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.core.ActivityHijack.ActProxyMgr.STUB_DEFAULT_ACTIVITY
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.getStaticFieldValueAs
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.qauxv.lifecycle.Parasitics

@ModuleEntry(
    id = "activity_hijack",
)
object ActivityHijack : BaseModule(), Core {
    override fun onLoad(): Boolean {
        loaderContext.application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (ActProxyMgr.isModuleProxyActivity(activity.localClassName)) {
                    val activityMeta = activity as? ModuleActivityMeta ?: return
                    activity.setTheme(activityMeta.theme)
                    activity.requestedOrientation = activityMeta.orientation
                }
            }

            override fun onActivityDestroyed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}
        })
        Parasitics.initForStubActivity(loaderContext.application)
        val success = Parasitics::class.java.getStaticFieldValueAs<Boolean>("__stub_hooked")
        if (success.not()) {
            Toast.show("Activity 劫持失败\n应用可能不稳定")
        }
        return success
    }

    object Log {
        @JvmStatic
        fun i(message: Any) {
            logger.i(message)
        }

        @JvmStatic
        fun e(message: Any) {
            logger.e(message)
        }
    }

    object MainHook

    object Initiator {
        @JvmStatic
        fun getPluginClassLoader(): ClassLoader? {
            return this::class.java.classLoader
        }

        @JvmStatic
        fun getHostClassLoader(): ClassLoader {
            return loaderContext.hostClassloader
        }
    }

    object HostInfo {

        @JvmStatic
        fun getApplication(): Application {
            return loaderContext.application
        }

        @JvmStatic
        fun getPackageName(): String {
            return loaderContext.application.packageName
        }
    }

    object ActProxyMgr {
        const val STUB_DEFAULT_ACTIVITY = "tv.danmaku.bili.mod.ModLocalInfoActivity"
        const val STUB_TRANSLUCENT_ACTIVITY = "com.mall.ui.page.base.TranslucentActivity"
        const val STUB_TOOL_ACTIVITY = STUB_DEFAULT_ACTIVITY

        const val ACTIVITY_PROXY_INTENT = "io.github.duzhaokun123.yabr.ACTIVITY_PROXY_INTENT"

        @JvmStatic
        fun isModuleProxyActivity(name: String): Boolean {
            return ModuleActivities.activities.find { it == name } != null
        }

        @JvmStatic
        fun isModuleBundleClassLoaderRequired(name: String): Boolean {
            return isModuleProxyActivity(name)
        }
    }

    interface WindowIsTranslucent

    object CounterfeitActivityInfoFactory {
        @JvmStatic
        fun makeProxyActivityInfo(className: String, flags: Long): ActivityInfo? {
            val activityInfo = loaderContext.application.packageManager.getActivityInfo(
                ComponentName(loaderContext.application, STUB_DEFAULT_ACTIVITY), flags.toInt())
            activityInfo.apply {
                targetActivity = null
                taskAffinity = null
                descriptionRes = 0
                name = className
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    splitName = null
                }
            }
            return activityInfo
        }
    }

    object StartupInfo {
        @JvmStatic
        fun getModulePath(): String {
            return loaderContext.modulePath
        }
    }

    object R {
        object string {
            @JvmField
            val res_inject_success = AppR.string.res_inject_success
        }
    }

    object SyncUtils {
        @JvmStatic
        fun runOnUiThread(r: Runnable) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                r.run()
            } else {
                Toast.handler.post(r)
            }
        }
    }
}

@Target(AnnotationTarget.CLASS)
annotation class ModuleActivity

interface ModuleActivityMeta {
    val theme: Int
        get() = AppR.style.AppTheme

    val orientation: Int
        get() = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}
