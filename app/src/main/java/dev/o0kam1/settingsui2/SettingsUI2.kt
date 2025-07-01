package dev.o0kam1.settingsui2

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.loadClass

@ModuleEntry(
    id = "dev.o0kam1.ui.SettingsUI2",
    targets = [ModuleEntryTarget.MAIN]
)
object SettingsUI2 : BaseModule() {
    override val canUnload = false
    override fun onLoad(): Boolean {
        loadClass("com.bilibili.app.preferences.BiliPreferencesActivity\$BiliPreferencesFragment")
            .findMethod { it.name == "onCreatePreferences" }
            .hookAfter {
                logger.d("here")
            }
        return true
    }
}