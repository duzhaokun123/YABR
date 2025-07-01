package dev.o0kam1.settingsui2

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget

@ModuleEntry(
    id = "dev.o0kam1.ui.SettingsUI2",
    targets = [ModuleEntryTarget.MAIN]
)
object SettingsUI2 : BaseModule() {
    override val canUnload = false
    override fun onLoad(): Boolean {
        return false
    }
}