package dev.o0kam1.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.module.core.ConfigStore
import io.github.duzhaokun123.yabr.module.core.PegasusHook
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs
import io.github.duzhaokun123.yabr.utils.loadMethod
import io.github.duzhaokun123.yabr.utils.setJsonFieldValue

data class PegasusHolderInfo(
    val type: String,
    val bizType: Any?,
    val className: String
)

@ModuleEntry(
    id = "dev.o0kam1.ui.PegasusItemTypeFilter",
    targets = [ModuleEntryTarget.MAIN]
)
object PegasusItemTypeFilter : BaseModule(), SwitchModule, UIComplex {
    override val name = "首页卡片类型过滤"
    override val description = "移除指定类型卡片"
    override val category = UICategory.UI

    val pegasusHolderTypes = mutableMapOf<String, PegasusHolderInfo>()

    override fun onLoad(): Boolean {
        val method_PegasusHolderData_getHolderType =
            loadMethod("Lcom/bilibili/pegasus/PegasusHolderData;->getHolderType()Ljava/lang/String;")
        val method_PegasusHolderData_getBizType =
            loadMethod("Lcom/bilibili/pegasus/PegasusHolderData;->getBizType()Lcom/bilibili/pegasus/BizType;")
        val config = ConfigStore.ofModule(this)
        PegasusHook.addInterceptFirst(id) { data ->
            val toRemove = config.getStringSet("remove") ?: emptySet()
            val items = data.getJsonFieldValueAs<MutableList<Any>>("items")
            var count = 0
            items.removeAll { item ->
                val itemClass = item.javaClass
                val type = method_PegasusHolderData_getHolderType.invoke(item) as String
                val bizType = method_PegasusHolderData_getBizType.invoke(item)
                pegasusHolderTypes[type] = PegasusHolderInfo(type, bizType, itemClass.name)
                val remove = type in toRemove
                if (remove) count++
                remove
            }
            if (config.getBoolean("show_count", false) == true) {
                val configData = data.getJsonFieldValueAs<Any>("config")
                val toastConfig = configData.getJsonFieldValueAs<Any>("toast")
                var toastMessage = toastConfig.getJsonFieldValueAs<String?>("toast_message") ?: ""
                toastMessage = toastMessage.trim().removeSuffix("\n")
                if (toastMessage.isNotEmpty()) toastMessage += "\n"
                toastMessage += "PegasusItemTypeFilter removed $count"
                toastConfig.setJsonFieldValue("toast_message", toastMessage)
                toastConfig.setJsonFieldValue("has_toast", true)
            }
        }
        return true
    }

    override fun onUnload(): Boolean {
        PegasusHook.removeIntercept(id)
        return super.onUnload()
    }



    override fun onCreateUI(context: Context): View {
        val config = ConfigStore.ofModule(this)
        val toRemove = (config.getStringSet("remove") ?: emptySet()).toMutableSet()

        val ll = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        val sw = Switch(context).apply {
            text = "启用首页卡片过滤"
            isChecked = this@PegasusItemTypeFilter.isEnabled
            setOnCheckedChangeListener { _, isChecked ->
                this@PegasusItemTypeFilter.isEnabled = isChecked
                if (isChecked && pegasusHolderTypes.isEmpty()) {
                    Toast.show("刷新首页以获取数据")
                }
            }
        }
        ll.addView(sw)
        for (type in (pegasusHolderTypes.keys + toRemove)) {
            val cb_item = CheckBox(context).apply {
                @SuppressLint("SetTextI18n")
                text = "$type\n${pegasusHolderTypes[type]}"
                isChecked = type in toRemove
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        toRemove.add(type)
                    } else {
                        toRemove.remove(type)
                    }
                    config.putStringSet("remove", toRemove)
                }
            }
            ll.addView(cb_item)
        }

        ll.addView(TextView(context).apply {
            @SuppressLint("SetTextI18n")
            text = "显然如果所有类型都被过滤导致加载内容为空集首页加载会出错"
        })
        ll.addView(Switch(context).apply {
            text = "显示过滤计数"
            isChecked = config.getBoolean("show_count", false) == true
            setOnCheckedChangeListener { _, isChecked ->
                config.putBoolean("show_count", isChecked)
            }
        })

        return ll
    }
}