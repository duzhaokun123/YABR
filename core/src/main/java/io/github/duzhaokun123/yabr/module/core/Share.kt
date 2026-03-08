package io.github.duzhaokun123.yabr.module.core

import android.net.Uri
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.lazyLoadClass
import io.github.duzhaokun123.yabr.utils.invokeStaticMethodAs
import io.github.duzhaokun123.yabr.utils.loaderContext

@ModuleEntry(
    id = "share",
)
object Share : BaseModule(), Core {
    val class_FileProvider by lazyLoadClass("androidx.core.content.FileProvider")

    override fun onLoad(): Boolean {
        // TODO: 检查必要的文件提供者
        return true
    }

    fun makeShareFileUri(data: ByteArray, suffix: String): Uri {
        val shareFile = loaderContext.application.cacheDir
                .resolve("op_cover_image")
                .resolve("tabrshare.${suffix.removePrefix(".")}")
        shareFile.parentFile?.mkdirs()
        shareFile.writeBytes(data)
        val shareUri = class_FileProvider.invokeStaticMethodAs<Uri>(
            "getUriForFile",
            loaderContext.application, "tv.danmaku.bili.fileprovider", shareFile
            )
        return shareUri
    }
}