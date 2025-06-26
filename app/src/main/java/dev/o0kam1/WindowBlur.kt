package dev.o0kam1

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.R
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.base.multiLoadAllSuccess
import io.github.duzhaokun123.yabr.module.base.requireMinSystem
import io.github.duzhaokun123.yabr.utils.SimpleActivityLifecycleCallbacks
import io.github.duzhaokun123.yabr.utils.findConstructor
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.loaderContext
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

    val backgroundBlurRadius = 40

    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onLoad() = multiLoadAllSuccess(::hookDialog, ::hookActivity)

    @RequiresApi(Build.VERSION_CODES.S)
    fun hookDialog(): Boolean {
        Dialog::class.java
            .findMethod { it.name == "onStart" }
            .hookAfter {
                val dialog = it.thiz as Dialog
                val window = dialog.window!!
                window.apply {
                    setBackgroundBlurRadius(backgroundBlurRadius)
                    val background = ContextCompat.getDrawable(dialog.context, R.drawable.dialog_background)!!
                    setBackgroundDrawable(background)
                    attributes.apply {
                        addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    }
                    val blurEnableListener = Consumer<Boolean> { enable: Boolean ->
                        if (enable) {
                            background.alpha = (255 * 0.50).toInt()
                            setDimAmount(0.2F)
                        } else {
                            background.alpha = (255 * 1.0).toInt()
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
            .findConstructor { it.parameterTypes contentEquals arrayOf(Context::class.java, Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType) }
            .hookBefore {
                val newThemeId = R.style.AppTheme_Dialog
                val newContext = ContextThemeWrapper(it.args[0] as Context, newThemeId)
                it.args[0] = newContext
                it.args[1] = newThemeId
                it.args[2] = false
            }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun hookActivity(): Boolean {
        loaderContext.application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) { // XXX: 被多次调用 但总比 onActivityPostCreated 不被调用好
                val a = activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowIsTranslucent))
                val windowIsTranslucent = a.getBoolean(0, false)
                a.recycle()
                if (windowIsTranslucent.not()) return
                activity.window.apply {
                    setBackgroundBlurRadius(backgroundBlurRadius)
                }
            }
        })
        return true
    }
}
