package dev.o0kam1

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.hookerContext
import kotlin.reflect.jvm.javaMethod

@ModuleEntry(
    id = "dev.o0kam1.CopyHook"
)
object CopyHook: BaseModule(), UISwitch, SwitchModule {
    override val name = "拦截复制"
    override val description = "转复制操作为自由复制"
    override val category = UICategory.TOOL

    override fun onLoad(): Boolean {
        ClipboardManager::class.java
            .findMethod { it.name == "setPrimaryClip" }
            .hookBefore {
                val isMyCopy = Throwable().stackTrace.find {
                    it.className == "android.widget.TextView" && it.methodName == "setPrimaryClip"
                } != null
                if (isMyCopy) return@hookBefore
                handleCopy(it.args[0] as ClipData)
                it.result = null
            }
        return true
    }

    fun handleCopy(clip: ClipData) {
        val activity = ActivityUtils.topActivity
        if (activity == null) {
            Toast.show("没有 activity 可用")
            return
        }

        AlertDialog.Builder(activity)
            .setTitle("自由复制")
            .setMessage(clip.getItemAt(0).text)
            .setPositiveButton("复制原始") { _, _ ->
                val cm = activity.getSystemService(ClipboardManager::class.java)
                hookerContext.invokeOriginal(ClipboardManager::setPrimaryClip.javaMethod!!, cm, clip)
            }.show()
            .apply {
                findViewById<TextView>(android.R.id.message).setTextIsSelectable(true)
            }
    }
}