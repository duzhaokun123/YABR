package dev.o0kam1

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import io.github.duzhaokun123.hooker.base.thisObject
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.R
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.base.requireMinSystem
import io.github.duzhaokun123.yabr.utils.findConstructor
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.paramCount
import java.util.function.Consumer

@ModuleEntry(
    id = "dev.o0kam1.WindowBlur",
)
object WindowBlur : BaseModule(), UISwitch, SwitchModule, Compatible {
    override val name = "窗口模糊"
    override val description = "比 \"全局 Dialog 背景模糊\" 还花里胡哨!\n" +
            "为所有 dialog 添加透明度\n" +
            "为所有 dialog 窗口和透明 activity 添加模糊层"
    override val category = UICategory.UI

    override fun checkCompatibility() = requireMinSystem(Build.VERSION_CODES.S)

    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onLoad(): Boolean {
        Dialog::class.java
            .findMethod { it.name == "onStart" }
            .hookAfter {
                val window = (it.thisObject as Dialog).window!!
                window.apply {
                    setBackgroundBlurRadius(40)
                    setBackgroundDrawableResource(R.drawable.dialog_background)
                    attributes.apply {
                        addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    }
                    val blurEnableListener = Consumer<Boolean> { enable: Boolean ->
                        if (enable) {
                            setDimAmount(0.2F)
                        } else {
                            setDimAmount(0.6F)
                        }
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
                }
            }
        Dialog::class.java
            .findConstructor { it.paramCount == 3
                    && it.parameterTypes contentEquals arrayOf(Context::class.java, Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType) }
            .hookBefore {
                val resId = it.args[1] as Int
                val createContextThemeWrapper = it.args[2] as Boolean
                logger.d("$resId $createContextThemeWrapper")
                val newThemeId = R.style.AppTheme_Dialog
                if (createContextThemeWrapper) {
                    it.args[1] = newThemeId
                } else {
                    val newContext = ContextThemeWrapper(it.args[0] as Context, newThemeId)
                    it.args[0] = newContext
                }
            }
        return true
    }
}
