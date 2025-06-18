package io.github.duzhaokun123.yabr.module.tool

import android.content.Context
import android.view.View
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.hookerContext
import io.github.duzhaokun123.yabr.utils.loaderContext

@ModuleEntry(
    id = "implementation_info",
    targets = [ModuleEntryTarget.MAIN]
)
object ImplementationInfo : BaseModule(), Core, UIComplex {
    override val name = "实现信息"
    override val description = "显示当前实现的信息"
    override val category = UICategory.TOOL

    override fun onLoad(): Boolean {
        return true
    }

    override fun onCreateUI(context: Context): View {
        return TextView(context).apply {
            val loaderInfo = loaderContext.implementationInfo
            append("Loader:\n")
            append("${loaderInfo.name} (${loaderInfo.version})\n")
            append("${loaderInfo.description}\n\n")
            val hookerInfo = hookerContext.implementationInfo
            append("Hooker:\n")
            append("${hookerInfo.name} (${hookerInfo.version})\n")
            append("${hookerInfo.description}\n")
        }
    }
}