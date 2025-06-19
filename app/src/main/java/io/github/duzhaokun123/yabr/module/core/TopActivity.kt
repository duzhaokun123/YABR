package io.github.duzhaokun123.yabr.module.core

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.utils.loaderContext

@SuppressLint("StaticFieldLeak")
@ModuleEntry(
    id = "top_activity"
)
object TopActivity: BaseModule(), Core {
    override val canUnload = false

    /**
     * 不考虑 multi resume, 总是最后一个 resume 的 Activity
     * 在多窗口下会有问题
     *
     * 导致 activity 泄露
     */
    var topActivity: Activity? = null
        private set

    override fun onLoad(): Boolean {
        loaderContext.application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity, savedInstanceState: Bundle?
                ) {

                }

                override fun onActivityDestroyed(activity: Activity) {

                }

                override fun onActivityPaused(activity: Activity) {

                }

                override fun onActivityResumed(activity: Activity) {
                    logger.v("onActivityResumed: ${activity}")
                    topActivity = activity
                }

                override fun onActivitySaveInstanceState(
                    activity: Activity, outState: Bundle
                ) {

                }

                override fun onActivityStarted(activity: Activity) {

                }

                override fun onActivityStopped(activity: Activity) {

                }
            }
        )
        return true
    }
}