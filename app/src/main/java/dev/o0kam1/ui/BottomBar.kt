package dev.o0kam1.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Switch
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.module.core.ConfigStore
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.getFieldValueAs
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loadMethod

@ModuleEntry(
    id = "dev.o0kam1.ui.BottomBar",
    targets = [ModuleEntryTarget.MAIN]
)
object BottomBar : BaseModule(), SwitchModule, UIComplex {
    override val name = "自定义底栏"
    override val description = "修改底栏项目"
    override val category = UICategory.UI

    override fun onLoad(): Boolean {
        val class_TabResponse =
            loadClass($$"Ltv/danmaku/bili/ui/main2/resource/MainResourceManager$TabResponse;")
        val method_JSON_parseObject_String_Type_int_LFeature =
            loadMethod("Lcom/alibaba/fastjson/JSON;->parseObject(Ljava/lang/String;Ljava/lang/reflect/Type;I[Lcom/alibaba/fastjson/parser/Feature;)Ljava/lang/Object;")
        val config = ConfigStore.ofModule(this)
        method_JSON_parseObject_String_Type_int_LFeature.hookAfter {
            if (it.result?.javaClass == class_TabResponse) {
                val ids = mutableSetOf<String>()
                val data = it.result!!.getFieldValueAs<Any>("tabData")
                val toRemove = config.getStringSet("remove") ?: emptySet()
                data.getFieldValueAs<MutableList<*>>("bottom").removeAll {
                    val uri = it!!.getFieldValueAs<String>("uri")
                    val id = it.getFieldValueAs<String>("tabId")
                    val name = it.getFieldValueAs<String>("name")
                    config.putString(id, "$name ($uri)")
                    ids.add(id)
                    id in toRemove
                }
                config.putStringSet("ids", ids)
            }
        }
        return true
    }

    override fun onCreateUI(context: Context): View {
        val config = ConfigStore.ofModule(this)
        val ids = config.getStringSet("ids") ?: emptySet()
        val toRemove = (config.getStringSet("remove") ?: emptySet()).toMutableSet()

        val ll = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        val sw = Switch(context).apply {
            text = "启用自定义底栏"
            isChecked = this@BottomBar.isEnabled
            setOnCheckedChangeListener { _, isChecked ->
                this@BottomBar.isEnabled = isChecked
                if (isChecked && ids.isEmpty()) {
                    Toast.show("重启应用以加载底栏数据")
                }
            }
        }
        ll.addView(sw)
        for (id in ids) {
            val cb_item = CheckBox(context).apply {
                @SuppressLint("SetTextI18n")
                text = "${config.getString(id)} ($id)"
                isChecked = id !in toRemove
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        toRemove.remove(id)
                    } else {
                        toRemove.add(id)
                    }
                    config.putStringSet("remove", toRemove)
                }
            }
            ll.addView(cb_item)
        }
        return ll
    }
}