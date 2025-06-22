package com.example

import android.app.Activity
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.paramCount

@ModuleEntry(
    id = "com.example.Example",
)
object Example : BaseModule(), UISwitch, SwitchModule {
//    override val canUnload = true
    override val name = "Example Module"
    override val description = "This is an example module for demonstration purposes."
    override val category = UICategory.FUN

    override fun onLoad(): Boolean {
        Activity::class.java
            .findMethod { it.name == "onCreate" && it.paramCount == 1 }
            .hookAfter {
                logger.d(it.thiz)
                Toast.show(it.thiz.toString())
            }
        return true
    }

//    override fun onUnload(): Boolean {
//        return super.onUnload()
//    }
}