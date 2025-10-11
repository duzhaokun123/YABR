package io.github.duzhaokun123.yabr.module.debug

import android.app.Activity
import android.content.Context
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.UIClick
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import io.github.duzhaokun123.yabr.utils.loaderContext
import kotlin.system.exitProcess

@ModuleEntry(
    id = "restart_app",
)
object RestartApp : BaseModule(), UIClick {
    override val name = "重启应用"
    override val description = "立即重启应用"
    override val category = UICategory.DEBUG

    override fun onLoad(): Boolean {
        return true
    }

    fun restart() : Nothing {
        logger.w("Restarting app...")
        val context = ActivityUtils.topActivity ?: loaderContext.application
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(context.packageName)
        if (context is Activity) {
            context.finishAffinity()
        }
        context.startActivity(intent)
        exitProcess(0)
    }

    override fun onClick(context: Context) {
        restart()
    }
}