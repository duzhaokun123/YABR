package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.utils.loaderContext

@ModuleEntry(
    id = "bili_info"
)
object BiliInfo: BaseModule(), Core {
    override fun onLoad(): Boolean {
        return false
    }

    val pmPackageName: String
        get() = loaderContext.application.packageName

}