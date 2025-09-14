package dev.o0kam1.tools

import android.annotation.SuppressLint
import android.content.Intent
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import io.github.duzhaokun123.yabr.module.core.ThreePointCallback
import io.github.duzhaokun123.yabr.module.core.ThreePointHook
import io.github.duzhaokun123.yabr.module.core.ThreePointItemItemData
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@ModuleEntry(
    id = "dev.o0kam1.tools.ThreePointShareLink"
)
object ThreePointShareLink: BaseModule(), UISwitch, SwitchModule {
    override val name = "三点分享链接"
    override val description = "长按菜单添加分享选项"
    override val category = UICategory.TOOL

    const val SHARE_ID = 529358L

    override fun onLoad(): Boolean {
        ThreePointHook.addThreePointCallback(
            SHARE_ID,
            object : ThreePointCallback {
                override fun parseData(data: Any): ThreePointItemItemData? {
                    runCatching {
                        val title = data.getJsonFieldValueAs<String>("title")
                        val link = data.getJsonFieldValueAs<String>("uri")
                        return ThreePointItemItemData(
                            name = "分享链接",
                            data = Json.encodeToString(Item(title, link))
                        )
                    }
                    return null
                }

                override fun onClick(data: ThreePointItemItemData) {
                    val item = Json.decodeFromString<Item>(data.data!!)
                    val intent = Intent.createChooser(Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, item.title)
                        putExtra(Intent.EXTRA_TEXT, item.link)
                    }, "分享链接")
                    ActivityUtils.topActivity!!.startActivity(intent)
                }
            }
        )
        return true
    }

    override fun onUnload(): Boolean {
        ThreePointHook.removeThreePointCallback(SHARE_ID)
        return super.onUnload()
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class Item(
        val title: String,
        val link: String
    )
}
