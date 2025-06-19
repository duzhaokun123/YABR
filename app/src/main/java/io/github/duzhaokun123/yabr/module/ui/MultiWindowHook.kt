package io.github.duzhaokun123.yabr.module.ui

import android.app.Activity
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findAllMethods

@ModuleEntry(
    id = "fake_non_multi_window_hook",
    targets = [ModuleEntryTarget.MAIN]
)
object FakeNonMultiWindowHook : BaseModule(), UISwitch, SwitchModule {
    override val name = "假装不是多窗口"
    override val description = "允许非全屏下的一些操作"
    override val category = UICategory.UI

    override fun onLoad(): Boolean {
        Activity::class.java
            .getDeclaredMethod("isInMultiWindowMode")
            .hookReplace { false }
        Activity::class.java
            .findAllMethods { it.name == "onMultiWindowModeChanged" }
            .hookBefore {
                it.args[0] = false
            }
        return true
    }
}