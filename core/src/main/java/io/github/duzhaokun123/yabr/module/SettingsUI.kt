package io.github.duzhaokun123.yabr.module

import android.annotation.SuppressLint
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
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIActivity
import io.github.duzhaokun123.yabr.module.base.UIClick
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.base.UIEntry
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.module.base.multiLoadAllSuccess
import io.github.duzhaokun123.yabr.module.core.SwitchModuleManager
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.dp
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.getFieldValue
import io.github.duzhaokun123.yabr.utils.getFieldValueOrNullAs
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.new
import io.github.duzhaokun123.yabr.utils.paramCount
import io.github.duzhaokun123.yabr.utils.setFieldValue
import io.github.duzhaokun123.yabr.utils.toMethod
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object UICategory {
    const val TOOL = "tool"
    const val UI = "ui"
    const val ABOUT = "about"
    const val FUN = "fun"
    const val DEBUG = "debug"

    val ORDER = listOf(TOOL, UI, FUN, DEBUG, ABOUT)
}

@ModuleEntry(
    id = "settings_ui",
    targets = [ModuleEntryTarget.MAIN]
)
object SettingsUI : BaseModule(), Core, DexKitMemberOwner {
    const val START_SETTING_KEY = "biliroaming_start_setting"
    const val SETTINGS_ID = 23232323L
    const val SETTINGS_URI = "bilibili://yabr_settings"
    const val ACTION_SETTINGS = "io.github.duzhaokun123.yabr.action.SETTINGS"

    override val canUnload = false

    val class_MenuGroupItem by lazy { loadClass("com.bilibili.lib.homepage.mine.MenuGroup\$Item") }

    val method_addSetting by dexKitMember(
        "tv.danmaku.bili.ui.main2.mine.HomeUserCenterFragment.addSetting"
    ) { bridge ->
        val class_HomeUserCenterFragment = loadClass("tv.danmaku.bili.ui.main2.mine.HomeUserCenterFragment")
        bridge.findMethod {
            matcher {
                declaredClass(class_HomeUserCenterFragment)
                paramTypes(
                    class_HomeUserCenterFragment,
                    loadClass("tv.danmaku.bili.ui.main2.api.AccountMine")
                )
                usingStrings("activity://main/preference")
            }
        }.single().toMethod()
    }

    val class_SettingsRouter by dexKitMember(
        "SettingsRouter"
    ) { bridge ->
        bridge.findMethod {
            matcher {
                paramTypes(class_MenuGroupItem)
                returnType(loadClass("com.bilibili.lib.homepage.mine.IMinePageInfo"))
                usingStrings("bilibili://main/drawer/upper-hot", "bilibili://main/drawer/upper-upload")
            }
        }.single().toMethod().declaringClass
    }

    private var startSettings = false

    override fun onLoad() =
        multiLoadAllSuccess(::broadcastOpen, ::startActivityOpen, ::minePageOpen)

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun broadcastOpen(): Boolean {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?, intent: Intent?
            ) {
                logger.i("Received settings action")
                showSettings()
            }
        }
        val filter = IntentFilter(ACTION_SETTINGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            loaderContext.application.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            loaderContext.application.registerReceiver(receiver, filter)
        }
        return true
    }

    fun startActivityOpen(): Boolean {
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
        return true
    }

    fun minePageOpen(): Boolean {
        method_addSetting?.hookBefore { param ->
            @Suppress("UNCHECKED_CAST")
            val list = param.args.getOrNull(1) as? MutableList<Any>
                ?: param.args.getOrNull(1)?.getFieldValueOrNullAs<MutableList<Any>>("moreSectionList")
                ?: param.args.getOrNull(1)?.getFieldValueOrNullAs<MutableList<Any>>("sectionListV2")
                ?: return@hookBefore
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
        class_SettingsRouter?.findMethod { it.paramCount == 1 && it.parameterTypes[0] == class_MenuGroupItem }
            ?.hookAfter { param ->
                val item = param.args[0] ?: return@hookAfter
                if (item.getFieldValue("uri") != SETTINGS_URI) return@hookAfter
                val pageInfoType = (param.method as Method).returnType
                param.result = Proxy.newProxyInstance(
                    pageInfoType.classLoader,
                    arrayOf(pageInfoType)
                ) { _, method, _ ->
                    return@newProxyInstance when (method.returnType.name) {
                        "com.bilibili.lib.homepage.mine.IMineMenuItemSolution" ->
                            Proxy.newProxyInstance(
                                method.returnType.classLoader,
                                arrayOf(method.returnType)
                            ) { _, solutionMethod, args ->
                                return@newProxyInstance when (solutionMethod.returnType) {
                                    Boolean::class.javaPrimitiveType -> false
                                    else -> {
                                        if (solutionMethod.name == "a" && args?.firstOrNull() is Context) {
                                            showSettings(args[0] as Context)
                                        }
                                        null
                                    }
                                }
                            }

                        "boolean" -> false
                        else -> null
                    }
                }
            }
        return true
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Suppress("UNCHECKED_CAST")
    fun showSettings(context: Context? = null) {
        var context = context ?: ActivityUtils.topActivity
        if (context == null) {
            logger.v("Context is null, skipping settings UI")
            Toast.show("无法获取当前上下文，无法显示设置界面")
            return
        }
//        context = context.createAppThemeWrapper()
        val scrollView = ScrollView(context)
        val listView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(listView, MATCH_PARENT, WRAP_CONTENT)
        val uiModule = (Main.allModule
            .filter { it is UIEntry } as List<UIEntry>).groupBy { it.category }
            .entries.sortedBy { 
                val index = UICategory.ORDER.indexOf(it.key)
                if (index == -1) Int.MAX_VALUE else index 
            }
        uiModule.forEach { (category, modules) ->
            logger.d("UI Category: $category")
            val header = when (category) {
                UICategory.UI -> "界面"
                UICategory.ABOUT -> "关于"
                UICategory.FUN -> "娱乐"
                UICategory.TOOL -> "工具"
                UICategory.DEBUG -> "调试"
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
                if (module is UIClick) {
                    relativeLayout.setOnClickListener {
                        runCatching {
                            module.onClick(context)
                        }.onFailure { t ->
                            logger.e("Failed to execute click action for module ${module.id}")
                            logger.e(t)
                            Toast.show("执行 ${module.name} 的点击操作失败: ${t.localizedMessage ?: t.message ?: "未知错误"}")
                        }
                    }
                }
                if (module is UIActivity) {
                    relativeLayout.setOnClickListener {
                        context.startActivity(Intent(context, module.moduleActivity))
                    }
                }
                relativeLayout.setOnLongClickListener {
                    val message = StringBuilder()
                    message.appendLine("ID: ${module.id}")
                    message.appendLine("Class: ${module.javaClass.name}")
                    message.appendLine("Name: ${module.name}")
                    message.appendLine("Description: ${module.description}")
                    message.appendLine("Implementation: ${module.javaClass.interfaces.joinToString(", ") { it.simpleName }}")
                    message.appendLine("Enabled: ${if (module is SwitchModule) module.isEnabled else "always"}")
                    if (module is Compatible) {
                        message.appendLine("Compatible: ${module.checkCompatibility() ?: "OK"}")
                    }
                    message.appendLine("Hooks: ${module.unhookers.count()}")
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
