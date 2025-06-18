package io.github.duzhaokun123.yabr.module

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitContext
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.base.UIEntry
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.module.core.SwitchModuleManager
import io.github.duzhaokun123.yabr.module.core.TopActivity
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.dp
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.getFieldValue
import io.github.duzhaokun123.yabr.utils.getFieldValueAs
import io.github.duzhaokun123.yabr.utils.getFieldValueOrNullAs
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.new
import io.github.duzhaokun123.yabr.utils.paramCount
import io.github.duzhaokun123.yabr.utils.setFieldValue
import io.github.duzhaokun123.yabr.utils.toClass
import io.github.duzhaokun123.yabr.utils.toMethod
import java.lang.reflect.Constructor
import java.lang.reflect.Proxy

object UICategory {
    const val TOOL = "tool"
    const val UI = "ui"
    const val ABOUT = "about"
}

@ModuleEntry(
    id = "settings_ui",
    targets = [ModuleEntryTarget.MAIN]
)
object SettingsUI : BaseModule(), Core, DexKitContext {
    const val START_SETTING_KEY = "biliroaming_start_setting"
    const val SETTINGS_ID = 23232323
    const val SETTINGS_URI = "bilibili://yabr_settings"
    const val ACTION_SETTINGS = "io.github.duzhaokun123.yabr.action.SETTINGS"

    override val canUnload = false

    val class_MenuGroupItem by lazy { loadClass("com.bilibili.lib.homepage.mine.MenuGroup\$Item") }

    val method_addSetting by dexKitMember(
        "tv.danmaku.bili.ui.main2.mine.HomeUserCenterFragment.addSetting"
    ) { bridge ->
        bridge.findMethod {
            matcher {
                declaredClass(loadClass("tv.danmaku.bili.ui.main2.mine.HomeUserCenterFragment"))
                paramTypes(Context::class.java, java.util.List::class.java, null)
            }
        }.single().toMethod()
    }

    val class_SettingsRouter by dexKitMember(
        "SettingsRouter"
    ) { bridge ->
        val class_UperHotMineSolution =
            bridge.findClass {
                matcher {
                    usingStrings("UperHotMineSolution")
                }
            }.single().interfaces.single()
        bridge.findClass {
            matcher {
                addFieldForType(class_UperHotMineSolution.name)
            }
        }.single().toClass()
    }

    private var startSettings = false

    override fun onLoad(): Boolean {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?, intent: Intent?
            ) {
                logger.i("Received settings action")
                showSettings()
            }
        }
        val filter = IntentFilter(ACTION_SETTINGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loaderContext.application.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            loaderContext.application.registerReceiver(receiver, filter)
        }

        val class_MainActivity = loadClass("tv.danmaku.bili.MainActivityV2")
        class_MainActivity
            .findMethod { it.name == "onCreate" && it.paramCount == 1 }
            .hookBefore {
                val thiz = it.thiz as Activity
                startSettings = thiz.intent.hasExtra(START_SETTING_KEY)
                (it.args[0] as Bundle?)?.remove("android:fragments")
            }
        class_MainActivity
            .findMethod { it.name == "onResume" }
            .hookAfter {
                if (startSettings) {
                    startSettings = false
                    showSettings()
                }
            }
        method_addSetting?.hookBefore { param ->
            @Suppress("UNCHECKED_CAST")
            val list = param.args[1] as? MutableList<Any>
                ?: param.args[1]?.getFieldValueAs<MutableList<Any>>("moreSectionList")!!
            val itemList = list.lastOrNull()?.let {
                if (it.javaClass != class_MenuGroupItem)
                    it.getFieldValueOrNullAs<MutableList<Any>>("itemList")
                else
                    list
            } ?: list
            itemList.forEach {
                if (it.getFieldValue("id") == SETTINGS_ID) return@hookBefore
            }
            val item = class_MenuGroupItem.new()
            item.apply {
                setFieldValue("id", SETTINGS_ID)
                setFieldValue("title", "YABR 设置")
                setFieldValue(
                    "icon",
                    "https://i0.hdslb.com/bfs/album/276769577d2a5db1d9f914364abad7c5253086f6.png"
                )
                setFieldValue("uri", SETTINGS_URI)
                setFieldValue("visible", 1)
            }
            itemList.add(item)
        }
        class_SettingsRouter?.hookAllConstructorsBefore { param ->
            if (param.args[1] != SETTINGS_URI) return@hookAllConstructorsBefore
            val routerType = (param.method as Constructor<*>).parameterTypes[3]
            param.args[3] = Proxy.newProxyInstance(
                routerType.classLoader,
                arrayOf(routerType)
            ) { _, method, _ ->
                val returnType = method.returnType
                return@newProxyInstance Proxy.newProxyInstance(
                    returnType.classLoader,
                    arrayOf(returnType)
                ) { _, method, args ->
                    return@newProxyInstance when (method.returnType) {
                        Boolean::class.javaPrimitiveType -> false
                        else -> {
                            if (method.parameterTypes.isNotEmpty() && method.parameterTypes[0].name == "android.app.Activity") {
                                showSettings(args[0] as Context)
                            }
                            null
                        }
                    }
                }
            }
        }
        return true
    }

    fun showSettings(context: Context? = null) {
        val context = context ?: TopActivity.topActivity!!
        val scrollView = ScrollView(context)
        val listView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(listView, MATCH_PARENT, WRAP_CONTENT)
        val uiModule = (Main.allModule
            .filter { it is UIEntry } as List<UIEntry>).groupBy { it.category }
        uiModule.forEach { (category, modules) ->
            logger.d("UI Category: $category")
            val header = when (category) {
                UICategory.UI -> "界面"
                UICategory.ABOUT -> "关于"
                else -> category
            }
            val headerView = TextView(context).apply {
                text = header
            }
            listView.addView(headerView)
            listView.addView(View(context).apply {
                setBackgroundColor(Color.GRAY)
            }, MATCH_PARENT, 1.dp)
            modules.forEach { module ->
                module as BaseModule
                logger.d("UI Module: ${module.id}(${module.name})")
                val relativeLayout = RelativeLayout(context)
                val name = TextView(context).apply {
                    text = module.name
                    id = View.generateViewId()
                }
                val description = TextView(context).apply {
                    text = module.description
                }
                relativeLayout.addView(name)
                relativeLayout.addView(
                    description, RelativeLayout.LayoutParams(
                        WRAP_CONTENT, WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.BELOW, name.id)
                    }
                )
                if (module is UISwitch) {
                    if (module !is SwitchModule) {
                        logger.e("Module ${module.id} is UISwitch but not SwitchModule")
                    } else {
                        val switch = Switch(context).apply {
                            isChecked = module.isEnabled
                        }
                        switch.setOnCheckedChangeListener { _, isChecked ->
                            SwitchModuleManager.setEnabled(module, isChecked)
                            if (isChecked) {

                            } else {
                                if (module.canUnload.not()) {
                                    Toast.show("禁用 ${module.name} 需要重启应用以生效")
                                }
                            }
                        }
                        relativeLayout.addView(
                            switch, RelativeLayout.LayoutParams(
                                WRAP_CONTENT, WRAP_CONTENT
                            ).apply {
                                addRule(RelativeLayout.ALIGN_PARENT_END)
                                addRule(RelativeLayout.CENTER_VERTICAL)
                            }
                        )
                        relativeLayout.setOnClickListener {
                            switch.isChecked = !switch.isChecked
                        }
                    }
                }
                if (module is UIComplex) {
                    relativeLayout.setOnClickListener {
                        runCatching {
                            val view = module.onCreateUI(context)
                            AlertDialog.Builder(context)
                                .setTitle(module.name)
                                .setView(view)
                                .setPositiveButton("close", null)
                                .setCancelable(false)
                                .show()
                        }.onFailure { t ->
                            logger.e("Failed to create UI for module ${module.id}")
                            logger.e(t)
                            Toast.show("创建 ${module.name} 的 UI 失败: ${t.localizedMessage ?: t.message ?: "未知错误"}")
                        }
                    }
                }
                relativeLayout.setOnLongClickListener {
                    val message = StringBuilder()
                    message.appendLine("ID: ${module.id}")
                    message.appendLine("Name: ${module.name}")
                    message.appendLine("Description: ${module.description}")
                    message.appendLine("Implementation: ${module.javaClass.interfaces.joinToString(", ") { it.simpleName }}")
                    if (module is SwitchModule) {
                        message.appendLine("Enabled: ${module.isEnabled}")
                    }
                    if (module is Compatible) {
                        message.appendLine("Compatible: ${module.checkCompatibility() ?: "OK"}")
                    }
                    AlertDialog.Builder(context)
                        .setTitle("Module Info")
                        .setMessage(message.toString())
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                        .findViewById<TextView>(android.R.id.message)?.apply {
                            setTextIsSelectable(true)
                        }
                    return@setOnLongClickListener true
                }
                listView.addView(relativeLayout, MATCH_PARENT, WRAP_CONTENT)
                listView.addView(View(context).apply {
                    setBackgroundColor(Color.GRAY)
                }, MATCH_PARENT, 1.dp)
            }
        }
        AlertDialog.Builder(context)
            .setTitle("YABR 设置")
            .setView(scrollView)
            .setPositiveButton("close") { _, _ ->

            }.setCancelable(false)
            .setView(scrollView)
            .show()
    }
}