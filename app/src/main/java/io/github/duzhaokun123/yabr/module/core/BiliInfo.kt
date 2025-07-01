package io.github.duzhaokun123.yabr.module.core

import android.os.Build
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.module.base.multiLoadAllSuccess
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loaderContext

data class BiliVersionInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
)

@ModuleEntry(
    id = "bili_info"
)
object BiliInfo : BaseModule(), Core, DexKitMemberOwner {
    override fun onLoad() = multiLoadAllSuccess(::loadPmVersionInfo, ::loadDexVersionInfo)

    lateinit var pmVersionInfo: BiliVersionInfo

    fun loadPmVersionInfo(): Boolean {
        val pm = loaderContext.application.packageManager
        val packageInfo = pm.getPackageInfo(loaderContext.application.packageName, 0)
        pmVersionInfo = BiliVersionInfo(
            packageName = packageInfo.packageName,
            versionName = packageInfo.versionName ?: "unknown",
            versionCode =
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
        )
        return true
    }

    val dexVersionName by dexKitMember(
        "dexVersionName",
    ) { bridge ->
        val class_SuspendProducer = loadClass("com.bilibili.lib.gripper.api.SuspendProducer")
        bridge.findMethod {
            matcher {
                usingStrings("infra.initBiliConfig", "ua")
                returnType(class_SuspendProducer)
            }
        }.single().usingStrings
            .find { it.startsWith("Mozilla") }!!
            .removePrefix("Mozilla/5.0 BiliDroid/")
            .removeSuffix(" (bbcallen@gmail.com)")
    }

    fun loadDexVersionInfo(): Boolean {
        logger.d("Dex version name: $dexVersionName")
        val versionCode = dexVersionName!!.split(".").map { it.toInt() }
            .let { it[0] * 1_000_000 + it[1] * 10_000 }

        return true
    }
}