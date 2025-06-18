package dev.o0kam1

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.core.Share
import io.github.duzhaokun123.yabr.module.core.ThreePointCallback
import io.github.duzhaokun123.yabr.module.core.ThreePointHook
import io.github.duzhaokun123.yabr.module.core.ThreePointItemItemData
import io.github.duzhaokun123.yabr.module.core.TopActivity
import io.github.duzhaokun123.yabr.utils.Http
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs
import io.github.duzhaokun123.yabr.utils.getResId
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
                    val cover = data.getJsonFieldValueAs<String>("cover")
                    return ThreePointItemItemData(
                        name = "获取封面",
                        data = cover
                    )
                }

                override fun onClick(data: ThreePointItemItemData) {
                    val cover = data.data
                    if (cover == null) {
                        Toast.show("封面获取失败")
                    } else {
                        logger.d(cover)
                        val activity = TopActivity.topActivity!!
                        val imageView = ImageView(activity)
                        var data = byteArrayOf()
                        AlertDialog.Builder(activity)
                            .setTitle(cover)
                            .setView(imageView)
                            .setPositiveButton("下载") { _, _ ->
                                if (data.isEmpty()) {
                                    Toast.show("封面未加载")
                                    return@setPositiveButton
                                }
                                runNewThread {
                                    val file =
                                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                            ?.resolve("BiliBili")
                                            ?.resolve("${System.currentTimeMillis()}.jpeg") // 可能不是 jpeg 猜的
                                    if (file == null) {
                                        Toast.show("无法获取存储目录")
                                    } else {
                                        runCatching {
                                            file.parentFile?.mkdirs()
                                            file.writeBytes(data)
                                            Toast.show("封面已保存到: ${file.absolutePath}")
                                        }.onFailure { t ->
                                            logger.e("Failed to save cover image")
                                            logger.e(t)
                                            Toast.show("封面保存失败: ${t.reason}")
                                        }
                                    }
                                }
                            }.setNegativeButton("分享") { _, _ ->
                                val shareUri = Share.makeShareFileUri(data, "jpeg")
                                activity.startActivity(Intent.createChooser(Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    setDataAndType(shareUri, activity.contentResolver.getType(shareUri))
                                }, "分享封面"))
                            }.show().apply {

                            }
                        runNewThread {
                            runCatching {
                                data = Http.get(cover).readAll()
                                val b = BitmapFactory.decodeByteArray(data, 0, data.size)
                                runMainThread {
                                    imageView.setImageBitmap(b)
                                }
                            }.onFailure { t ->
                                logger.e("Failed to load cover image")
                                logger.e(t)
                                Toast.show("封面加载失败: ${t.reason}")
                            }
                        }
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