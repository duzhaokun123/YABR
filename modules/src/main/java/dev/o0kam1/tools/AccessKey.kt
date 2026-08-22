package dev.o0kam1.tools

import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.compat.biliroaming.isStatic
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.UIClick
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs
import io.github.duzhaokun123.yabr.utils.invokeStatic
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.paramCount

@ModuleEntry(
    id = "dev.o0kam1.tools.AccessKey",
    targets = [ModuleEntryTarget.MAIN],
)
object AccessKey : BaseModule(), UIClick {
    override val name = "获取 AccessKey"
    override val description = "获取当前登录账号的 AccessKey"
    override val category = UICategory.TOOL

    override fun onLoad(): Boolean = true

    override fun onClick(context: Context) {
        val key = readAccessKey() ?: run {
            Toast.show("未获取到 AccessKey")
            return
        }

        AlertDialog.Builder(context)
            .setTitle("当前 AccessKey")
            .setMessage(key)
            .setPositiveButton("复制") { _, _ ->
                CopyHook.copyDirect(ClipData.newPlainText("AccessKey", key))
                Toast.show("AccessKey 已复制到剪贴板")
            }
            .show()
            .apply {
                findViewById<TextView>(android.R.id.message).apply {
                    typeface = Typeface.MONOSPACE
                    setTextIsSelectable(true)
                }
            }
    }

    private fun readAccessKey(): String? {
        val class_BiliAccounts = loadClass("com.bilibili.lib.accounts.BiliAccounts")
        val class_AccessToken = loadClass("com.bilibili.lib.accounts.model.AccessToken")
        val method_getAccessToken = class_BiliAccounts.findMethod { method ->
            method.isStatic && method.paramCount == 0 && method.returnType == class_AccessToken
        }
        val accessToken = method_getAccessToken.invokeStatic()!!
        return accessToken.getJsonFieldValueAs("access_token")
    }
}
