package io.github.duzhaokun123.yabr.module.core

import android.os.Build
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.UIEntry

@ModuleEntry(
    id = "system_info",
)
object SystemInfo : BaseModule(), Core, UIEntry {
    override val name = "系统信息"
    override val description =
        "Android $systemVersionName (API ${Build.VERSION.SDK_INT} (${Build.VERSION.SDK_INT_FULL})) ${Build.BRAND} ${Build.MODEL}"
    override val category = UICategory.ABOUT

    override fun onLoad(): Boolean {
        return true
    }

    private val systemVersionName: String
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Build.VERSION.RELEASE_OR_CODENAME
            } else {
                Build.VERSION.RELEASE
            }
}
