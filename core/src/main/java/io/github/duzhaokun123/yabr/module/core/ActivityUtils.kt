package io.github.duzhaokun123.yabr.module.core

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.utils.loaderContext

@SuppressLint("StaticFieldLeak")
@ModuleEntry(
    id = "top_activity"
)
object ActivityUtils: BaseModule(), Core {
    override val canUnload = false

    /**
     * 不考虑 multi resume 在多窗口下会有问题
     */
    val topActivity: Activity?
        get() = activities.findLast { it.window.decorView.windowVisibility == View.VISIBLE }

    val activities = mutableListOf<Activity>()

    override fun onLoad(): Boolean {
        loaderContext.application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity, savedInstanceState: Bundle?
                ) {
                    activities.add(activity)
                }

                override fun onActivityDestroyed(activity: Activity) {
                    activities.remove(activity)
                }

                override fun onActivityPaused(activity: Activity) {

                }

                override fun onActivityResumed(activity: Activity) {

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