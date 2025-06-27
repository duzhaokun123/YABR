package dev.o0kam1

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import android.widget.ImageView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.core.Share
import io.github.duzhaokun123.yabr.module.core.ThreePointCallback
import io.github.duzhaokun123.yabr.module.core.ThreePointHook
import io.github.duzhaokun123.yabr.module.core.ThreePointItemItemData
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import io.github.duzhaokun123.yabr.utils.Http
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.getFieldValueAs
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueOrNullAs
import io.github.duzhaokun123.yabr.utils.invokeMethodAs
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.readAll
import io.github.duzhaokun123.yabr.utils.reason
import io.github.duzhaokun123.yabr.utils.runMainThread
import io.github.duzhaokun123.yabr.utils.runNewThread

@ModuleEntry(
    id = "dev.o0kam1.Cover",
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
                        return ThreePointItemItemData(
                            name = "获取封面",
                            data = cover
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
                            data = cover
                        )
                    }
                    logger.d("unable get cover for ${data.javaClass}")
                    return null
                }

                override fun onClick(data: ThreePointItemItemData) {
                    val cover = data.data
                    if (cover == null) {
                        Toast.show("封面获取失败")
                    } else {
                        logger.d(cover)
                        val activity = ActivityUtils.topActivity!!
                        activity.startActivity(Intent(activity, PhotoActivity::class.java).apply {
                            putExtra(PhotoActivity.URL, cover)
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