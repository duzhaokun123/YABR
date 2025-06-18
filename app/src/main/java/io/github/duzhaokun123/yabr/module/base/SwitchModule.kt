package io.github.duzhaokun123.yabr.module.base

import io.github.duzhaokun123.yabr.module.core.SwitchModuleManager

interface SwitchModule

val SwitchModule.isEnabled: Boolean
    get() = SwitchModuleManager.isEnabled(this as BaseModule)

fun SwitchModule.enable() {
    SwitchModuleManager.setEnabled(this as BaseModule, true)
}

fun SwitchModule.disable() {
    SwitchModuleManager.setEnabled(this as BaseModule, false)
}