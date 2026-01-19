package io.github.duzhaokun123.yabr.module.core

import android.content.Context
import android.widget.Toast
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.toMethod

@ModuleEntry(
    id = "bili_toast"
)
object BiliToast : BaseModule(), Core, DexKitMemberOwner {
    val method_show by dexKitMember(
        "com.bilibili.droid.ToastHelper.showToast",
    ) { bridge ->
        val showCaller = bridge.findMethod {
            matcher {
                usingStrings("main.lessonmodel.enterdetail.change-pswd-success.click")
            }
        }.single()
        bridge.findMethod {
            matcher {
                addCaller(showCaller.descriptor)
                paramCount = 3
                paramTypes(Context::class.java, String::class.java, null)
            }
        }.single().toMethod()
    }

    override fun onLoad(): Boolean {
        return method_show != null
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) : Boolean {
        return runCatching {
            method_show!!.invoke(null, loaderContext.application, message, duration)
            true
        }.getOrDefault(false)
    }
}
