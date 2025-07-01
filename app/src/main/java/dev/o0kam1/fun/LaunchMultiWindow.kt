package dev.o0kam1.`fun`

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.paramCount

@ModuleEntry(
    id = "dev.o0kam1.LaunchMultiWindow",
)
object LaunchMultiWindow : BaseModule(), UISwitch, SwitchModule {
    override val name = "多窗口启动"
    override val description = "以多窗口新任务栈启动所有 activity, 能导致任何问题\n" +
            "FLAG_ACTIVITY_NEW_TASK, FLAG_ACTIVITY_MULTIPLE_TASK\n" +
            "KEY_LAUNCH_WINDOWING_MODE = WINDOWING_MODE_FREEFORM"
    override val category = UICategory.FUN

    override fun onLoad(): Boolean {
        Activity::class.java
            .findMethod { it.name == "startActivity" && it.paramCount == 2 }
            .hookBefore {
                val intent = it.args[0] as Intent
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                var options = it.args[1] as Bundle?
                if (options == null) {
                    options = ActivityOptions.makeBasic().toBundle()
                    it.args[1] = options
                }
                options.putInt(
                    "android.activity.windowingMode" /* KEY_LAUNCH_WINDOWING_MODE */,
                    5 /* WINDOWING_MODE_FREEFORM */
                )
            }
        return true
    }
}