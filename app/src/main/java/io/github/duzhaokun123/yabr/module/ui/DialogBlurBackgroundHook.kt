package io.github.duzhaokun123.yabr.module.ui

import android.app.Dialog
import android.os.Build
import android.view.View
import android.view.WindowManager
import io.github.duzhaokun123.hooker.base.thisObject
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.base.requireMinSystem
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findMethod

@ModuleEntry(
    id = "dialog_blur_background_hook",
    targets = [ModuleEntryTarget.MAIN, ModuleEntryTarget.WEB]
)
object DialogBlurBackgroundHook : BaseModule(), SwitchModule, UISwitch, Compatible {
    override val name = "全局 Dialog 背景模糊"
    override val description = "花里胡哨!"
    override val category = UICategory.UI

    override fun checkCompatibility() = requireMinSystem(Build.VERSION_CODES.S)

    override fun onLoad(): Boolean {
        Dialog::class.java
            .findMethod { it.name == "onStart" }
            .hookAfter {
                val window = (it.thisObject as Dialog).window ?: return@hookAfter
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@hookAfter
                window.apply {
                    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    attributes.blurBehindRadius = 50
                    setBackgroundBlurRadius(50)
                    val blurEnableListener = { enable: Boolean ->
                        setDimAmount(if (enable) 0.1F else 0.6F)
                    }
                    decorView.addOnAttachStateChangeListener(object :
                        View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            windowManager.addCrossWindowBlurEnabledListener(blurEnableListener)
                        }

                        override fun onViewDetachedFromWindow(v: View) {
                            windowManager.removeCrossWindowBlurEnabledListener(blurEnableListener)
                        }

                    })
                    addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }
            }
        return true
    }
}