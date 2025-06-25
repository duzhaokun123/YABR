package dev.o0kam1

import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.os.Build
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.base.requireMinSystem
import io.github.duzhaokun123.yabr.utils.findConstructor
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.getFieldValueAs
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.paramCount
import io.github.duzhaokun123.yabr.utils.setFieldValue

@ModuleEntry(
    id = "dev.o0kam1.PredictiveBack",
)
object PredictiveBack : BaseModule(), UISwitch, SwitchModule, Compatible {
    override val name = "预见试返回手势"
    override val description = "和那些通用模块没有区别\n" +
            "一样的破坏应用行为"
    override val category = UICategory.UI

    override fun checkCompatibility() = requireMinSystem(Build.VERSION_CODES.TIRAMISU)

    override fun onLoad(): Boolean {
        ApplicationInfo::class.java
            .hookAllConstructorsBefore {
                val appInfo = if (it.args.size == 1) it.args[0] else it.thiz
                appInfo!!
                runCatching {
                    var privateFlagsExt = appInfo.getFieldValueAs<Int>("privateFlagsExt")
                    privateFlagsExt =
                        privateFlagsExt or (1 shl 3) // PRIVATE_FLAG_EXT_ENABLE_ON_BACK_INVOKED_CALLBACK
                    appInfo.setFieldValue("privateFlagsExt", privateFlagsExt)
                }.onFailure { t ->
                    logger.e("Failed to enable predictive back gesture", t)
                }
            }
        loadClass("android.app.ActivityThread")
            .findMethod { it.name == "handleLaunchActivity" }
            .hookBefore {
                val activityRecord = it.args[0]!!
                val activityInfo = activityRecord.getFieldValueAs<ActivityInfo>("activityInfo")
                var privateFlags = activityInfo.getFieldValueAs<Int>("privateFlags")
                privateFlags =
                    privateFlags or (1 shl 2) // PRIVATE_FLAG_ENABLE_ON_BACK_INVOKED_CALLBACK
                privateFlags =
                    privateFlags and (1 shl 3).inv() // PRIVATE_FLAG_DISABLE_ON_BACK_INVOKED_CALLBACK
                activityInfo.setFieldValue("privateFlags", privateFlags)
            }
        return true
    }
}