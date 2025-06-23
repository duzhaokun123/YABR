package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.BuildConfig
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.UIEntry

@ModuleEntry(
    id = "module_version",
)
object ModuleVersion : BaseModule(), Core, UIEntry {
    override val name = "模块版本"
    override val description = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) ${BuildConfig.BUILD_TYPE} ${BuildConfig.BUILD_TIME}"
    override val category = UICategory.ABOUT

    override fun onLoad(): Boolean {
        return true
    }
}