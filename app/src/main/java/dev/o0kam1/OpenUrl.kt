package dev.o0kam1

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.core.net.toUri
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.R
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.reason

@ModuleEntry(
    id = "dev.o0kam1.OpenUrl",
    targets = [ModuleEntryTarget.MAIN]
)
object OpenUrl : BaseModule(), UIComplex {
    override val name = "打开 url"
    override val description = "在应用内打开任何 url, 比如 bilibili://"
    override val category = UICategory.TOOL

    override fun onLoad(): Boolean {
        return true
    }

    @SuppressLint("InflateParams")
    override fun onCreateUI(context: Context): View {
        val rootView = LayoutInflater.from(context).inflate(R.layout.module_openurl, null)
        val et_url = rootView.findViewById<EditText>(R.id.et_url)
        val btn_open = rootView.findViewById<Button>(R.id.btn_open)
        btn_open.setOnClickListener {
            runCatching {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.`package` = context.packageName
                intent.data = et_url.text.toString().toUri()
                context.startActivity(intent)
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