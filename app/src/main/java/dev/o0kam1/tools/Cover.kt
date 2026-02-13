package dev.o0kam1.tools

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
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.getFieldValueAs
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs

@ModuleEntry(
    id = "dev.o0kam1.tools.Cover",
    targets = [ModuleEntryTarget.MAIN]
)
object Cover : BaseModule(), SwitchModule, UISwitch {
    const val COVER_ID = 529357L

    override val name = "获取封面"
    override val description = "长按菜单添加获取封面选项"
    override val category = UICategory.TOOL

    override fun onLoad(): Boolean {
        ThreePointHook.addThreePointCallback(
            COVER_ID,
            object : ThreePointCallback {
                override fun parseData(data: Any): ThreePointItemItemData? {
                    runCatching {
                        val cover = data.getJsonFieldValueAs<String>("cover")
                        val title = data.getJsonFieldValueAs<String>("title")
                        return ThreePointItemItemData(
                            name = "获取封面",
                            data = "$cover\n$title"
                        )
                    }
//                    runCatching {
//                        val basicInfo = data.invokeMethodAs<Any>("getBasicInfo")
//                        val cover = basicInfo.invokeMethodAs<String>("getCover")
//                        return ThreePointItemItemData(
//                            name = "获取封面",
//                            data = cover
//                        )
//                    }
                    runCatching {
                        val basicInfo = data.getFieldValueAs<Any>("c")
                        val cover = basicInfo.getFieldValueAs<String>("c")
                        return ThreePointItemItemData(
                            name = "获取封面",
                            data = "$cover\n"
                        )
                    }
                    logger.d("unable get cover for ${data.javaClass}")
                    return null
                }

                override fun onClick(data: ThreePointItemItemData) {
                    val data = data.data
                    if (data == null) {
                        Toast.show("封面获取失败")
                    } else {
                        logger.d(data)
                        val cover = data.substringBefore("\n")
                        val title = data.substringAfter("\n")
                        val activity = ActivityUtils.topActivity!!
                        activity.startActivity(Intent(activity, PhotoActivity::class.java).apply {
                            putExtra(PhotoActivity.URL, cover)
                            putExtra(PhotoActivity.TITLE, title)
                        })
                    }
                }
            }
        )
        return true
    }

    override fun onUnload(): Boolean {
        ThreePointHook.removeThreePointCallback(COVER_ID)
        return super.onUnload()
    }
}