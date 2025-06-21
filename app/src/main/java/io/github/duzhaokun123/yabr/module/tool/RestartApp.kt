package io.github.duzhaokun123.yabr.module.tool

import android.app.Activity
import android.content.Context
import android.view.View
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import io.github.duzhaokun123.yabr.utils.loaderContext
import kotlin.system.exitProcess

@ModuleEntry(
    id = "restart_app",
)
object RestartApp : BaseModule(), UIComplex {
    override val name = "重启应用"
    override val description = "立即重启应用"
    override val category = UICategory.TOOL

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

    override fun onCreateUI(context: Context): View {
        restart()
    }
}