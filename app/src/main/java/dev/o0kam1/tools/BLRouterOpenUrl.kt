package dev.o0kam1.tools

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.R
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.invokeStatic
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.reason
import java.lang.reflect.Method

@ModuleEntry(
    id = "dev.o0kam1.tools.BLRouterOpenUrl",
    targets = [ModuleEntryTarget.MAIN]
)
object BLRouterOpenUrl: BaseModule(), UIComplex {
    override val name = "BLRouter 打开 url"
    override val description = "使用 BLRouter 打开任何 url, 比如 bilibili://"
    override val category = UICategory.TOOL

    lateinit var method_BLRouter_routeTo: Method
    lateinit var method_RouteRequestK_toRouteRequest: Method

    override fun onLoad(): Boolean {
        method_BLRouter_routeTo = loadClass("com.bilibili.lib.blrouter.BLRouter")
            .findMethod { it.name == "routeTo" && it.parameterTypes[1] == Context::class.java }
        method_RouteRequestK_toRouteRequest = loadClass("com.bilibili.lib.blrouter.RouteRequestKt")
            .findMethod { it.name == "toRouteRequest" && it.parameterTypes contentEquals arrayOf(String::class.java) }
        return true
    }

    override fun onCreateUI(context: Context): View {
        val rootView = LayoutInflater.from(context).inflate(R.layout.module_blrouter_openurl, null)
        val et_url = rootView.findViewById<EditText>(R.id.et_url)
        val btn_open = rootView.findViewById<Button>(R.id.btn_open)
        val tv_response = rootView.findViewById<TextView>(R.id.tv_response)
        btn_open.setOnClickListener {
            runCatching {
                val routeRequest = method_RouteRequestK_toRouteRequest.invokeStatic(et_url.text.toString())
                val routeResponse = method_BLRouter_routeTo.invokeStatic(routeRequest, context)
                tv_response.text = routeResponse.toString()
                Toast.show("已处理 ${et_url.text}")
            }.onFailure { t ->
                Toast.show("无法打开 ${et_url.text}\n${t.reason}")
            }
        }
        et_url.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    btn_open.callOnClick()
                    true
                }

                else -> false
            }
        }
        return rootView
    }
}