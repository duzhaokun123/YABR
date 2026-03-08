package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core

@ModuleEntry(
    id = "switch_module_manager"
)
object SwitchModuleManager : BaseModule(), Core {
    const val KEY_ENABLED_MODULES = "enabled_modules"

    override fun onLoad(): Boolean {
        return true
    }

    fun isEnabled(module: BaseModule): Boolean {
        return ConfigStore.global.getStringSet(KEY_ENABLED_MODULES)?.contains(module.id) ?: false
    }

    fun setEnabled(module: BaseModule, enabled: Boolean) {
        val enabledModules = ConfigStore.global.getStringSet(KEY_ENABLED_MODULES)?.toMutableSet() ?: mutableSetOf()
        if (enabled) {
            enabledModules.add(module.id)
            Main.loadModule(module)
        } else {
            enabledModules.remove(module.id)
            Main.unloadModule(module)
        }
        ConfigStore.global.putStringSet(KEY_ENABLED_MODULES, enabledModules)
    }
}