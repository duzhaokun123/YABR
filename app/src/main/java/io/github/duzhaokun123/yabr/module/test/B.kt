package io.github.duzhaokun123.yabr.module.test

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIEntry
import io.github.duzhaokun123.yabr.module.base.UISwitch

@ModuleEntry(
    id = "testb"
)
object B: BaseModule(), UISwitch, SwitchModule, Compatible {
    override fun onLoad(): Boolean {
        return true
    }

    override val name = "dev.o0kam1.Test B Module"
    override val description = "This is a test module for B."
    override val category = "test"

    override fun checkCompatibility(): String? {
        return "test"
    }
}