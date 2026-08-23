package dev.o0kam1.ui

import android.view.View
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.getResId
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.paramCount

@ModuleEntry(
    id = "dev.o0kam1.ui.SplashDarkMode",
)
object SplashDarkMode : BaseModule(), UISwitch, SwitchModule {
    override val name = "启动首屏暗色"
    override val description = "在深色模式下将启动首屏背景设置为深色"
    override val category = UICategory.UI

    override fun onLoad(): Boolean {
        val class_BaseBrandSplashFragment =
            loadClass("tv.danmaku.bili.ui.splash.brand.ui.BaseBrandSplashFragment")
        class_BaseBrandSplashFragment
            .findMethod { it.name == "onViewCreated" && it.paramCount == 2 }
            .hookAfter {
                val view = it.args[0] as View
                val a =
                    view.context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground))
                val backgroundColor = a.getColor(0, 0)
                a.recycle()
                val containerId = getResId("splash_container")
                val container = if (containerId != 0) view.findViewById<View>(containerId) else null
                (container ?: view).setBackgroundColor(backgroundColor)
            }
        return true
    }
}
