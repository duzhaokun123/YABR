package io.github.duzhaokun123.yabr.module.core

import android.content.Context
import android.widget.Toast
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loaderContext
import java.lang.reflect.Method

@ModuleEntry(
    id = "bili_toast"
)
object BiliToast : BaseModule(), Core {
    lateinit var method_show: Method

    override fun onLoad(): Boolean {
        method_show = loadClass("com.bilibili.droid.ToastHelper")
            .findMethod { it.parameterTypes contentEquals arrayOf(Context::class.java, String::class.java, Int::class.javaPrimitiveType) }
        return true
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) : Boolean {
        return runCatching {
            method_show.invoke(null, loaderContext.application, message, duration)
            true
        }.getOrDefault(false)
    }
}
