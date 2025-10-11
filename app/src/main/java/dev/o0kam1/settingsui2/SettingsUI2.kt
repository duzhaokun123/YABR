package dev.o0kam1.settingsui2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.bilibili.app.preferences.settings2.Settings2SwitchPreference
import com.bilibili.lib.ui.BaseFragment
import com.bilibili.lib.ui.BasePreferenceFragment
import dev.o0kam1.settingsui2.SettingsUI2.OnPreferenceChangeListener
import dev.o0kam1.settingsui2.SettingsUI2.OnPreferenceClickListener
import dev.o0kam1.settingsui2.SettingsUI2.PreferenceScreen
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.R
import io.github.duzhaokun123.yabr.logger.AndroidLogger
import io.github.duzhaokun123.yabr.module.SettingsUI
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIActivity
import io.github.duzhaokun123.yabr.module.base.UIClick
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.base.UIEntry
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.base.disable
import io.github.duzhaokun123.yabr.module.base.enable
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.invokeMethod
import io.github.duzhaokun123.yabr.utils.invokeMethodAs
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.paramCount
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@ModuleEntry(
    id = "dev.o0kam1.ui.SettingsUI2",
    targets = [ModuleEntryTarget.MAIN]
)
object SettingsUI2 : BaseModule() {
    override val canUnload = false

    lateinit var class_OnPreferenceClickListener: Class<*>
    lateinit var class_OnPreferenceChangeListener: Class<*>
    lateinit var method_PreferenceManager_createPreferenceScreen: Method

    override fun onLoad(): Boolean {
        val class_Preference = loadClass("androidx.preference.Preference")
        class_OnPreferenceClickListener =
            class_Preference
                .findMethod { it.name == "setOnPreferenceClickListener" }
                .parameterTypes[0]
        class_OnPreferenceChangeListener =
            class_Preference
                .findMethod { it.name == "setOnPreferenceChangeListener" }
                .parameterTypes[0]
        method_PreferenceManager_createPreferenceScreen =
            loadClass("androidx.preference.PreferenceFragmentCompat")
                .findMethod { it.name == "getPreferenceManager" }
                .returnType
                .findMethod { it.returnType == loadClass("androidx.preference.PreferenceScreen") && it.paramCount == 1 }
        loadClass("com.bilibili.app.preferences.BiliPreferencesActivity\$BiliPreferencesFragment")
            .findMethod { it.name == "onCreatePreferences" }
            .hookAfter {
                val preferenceFragment = it.thiz as BasePreferenceFragment
                val context = preferenceFragment.requireContext()
                val preferenceCategoryYABR = PreferenceCategory(context).apply {
                    title = "YABR"
                }
                preferenceFragment.preferenceScreen.addPreference(preferenceCategoryYABR)
                val preferenceYABRSettings1 = Preference(context).apply {
                    title = "YABR 设置 v1"
                    invokeMethod("setOnPreferenceClickListener", OnPreferenceClickListener {
                        SettingsUI.showSettings(context)
                        true
                    })
                }
                preferenceCategoryYABR.addPreference(preferenceYABRSettings1)
                val preferenceScreenYABRSettings2 =
                    PreferenceScreen(context, preferenceFragment).apply {
                        title = "YABR 设置 v2"
                        fragment = YABRSettings2Fragment::class.java.name
                    }
                preferenceFragment.preferenceScreen.addPreference(preferenceScreenYABRSettings2)
            }
        return true
    }

    fun OnPreferenceClickListener(callback: (Preference) -> Boolean): Any {
        return Proxy.newProxyInstance(
            loaderContext.hostClassloader,
            arrayOf(class_OnPreferenceClickListener)
        ) { _, _, args ->
            callback(args[0] as Preference)
        }
    }

    fun OnPreferenceChangeListener(callback: (Preference, Any?) -> Boolean): Any {
        return Proxy.newProxyInstance(
            loaderContext.hostClassloader,
            arrayOf(class_OnPreferenceChangeListener)
        ) { _, _, args ->
            callback(args[0] as Preference, args[1])
        }
    }

    fun PreferenceScreen(
        context: Context,
        preferenceFragment: PreferenceFragmentCompat
    ): PreferenceScreen {
        return preferenceFragment
            .invokeMethodAs<Any>("getPreferenceManager")
            .invokeMethodAs<PreferenceScreen>(
                method_PreferenceManager_createPreferenceScreen, context
            )
    }
}

class YABRSettings2Fragment : BasePreferenceFragment() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(R.xml.empty_preference)
        AndroidLogger.d("$preferenceScreen")
        val uiModule =
            Main.allModule
                .filter { it is UIEntry }
                .groupBy { (it as UIEntry).category }
        uiModule.forEach { (category, modules) ->
            val preference = Preference(requireContext()).apply {
                title = when (category) {
                    UICategory.TOOL -> "工具"
                    UICategory.UI -> "界面"
                    UICategory.ABOUT -> "关于"
                    UICategory.FUN -> "娱乐"
                    UICategory.DEBUG -> "调试"
                    else -> category
                }
                summary = category
                fragment = YABRSettings2ListFragment::class.java.name
                invokeMethod("setOnPreferenceClickListener", OnPreferenceClickListener {
                    YABRSettings2ListFragment.showModules = modules
                    false
                })
            }
            preferenceScreen.addPreference(preference)
        }
    }
}

class YABRSettings2ListFragment : BasePreferenceFragment() {
    companion object {
        var showModules = emptyList<BaseModule>()
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(R.xml.empty_preference)
        showModules.forEach { module ->
            module as UIEntry
            val preference = when (module) {
                is UISwitch -> Settings2SwitchPreference(requireContext())
                is UIComplex -> PreferenceScreen(requireContext(), this)
                is UIClick -> Preference(requireContext()).apply {
                    widgetLayoutResource = ResourcesCompat.ID_NULL
                }
                is UIActivity -> PreferenceScreen(requireContext(), this)
                else -> Preference(requireContext()).apply {
                    widgetLayoutResource = ResourcesCompat.ID_NULL
                }
            }
            preference.apply {
                title = module.name
                summary = module.description
            }
            if (module is SwitchModule) {
                preference.setDefaultValue(module.isEnabled)
                preference.invokeMethod(
                    "setOnPreferenceChangeListener",
                    OnPreferenceChangeListener { _, enable ->
                        if (enable == true) {
                            module.enable()
                        } else {
                            module.disable()
                        }
                        return@OnPreferenceChangeListener true
                    })
            }
            if (module is UIComplex) {
                preference.fragment = YABRSettings2UIComplexFragment::class.java.name
                preference.invokeMethod("setOnPreferenceClickListener", OnPreferenceClickListener {
                    YABRSettings2UIComplexFragment.showModule = module
                    return@OnPreferenceClickListener false
                })
            }
            if (module is UIClick) {
                preference.invokeMethod("setOnPreferenceClickListener", OnPreferenceClickListener {
                    module.onClick(requireContext())
                    return@OnPreferenceClickListener true
                })
            }
            if (module is UIActivity) {
                preference.intent = Intent(requireContext(), module.moduleActivity)
            }
            preferenceScreen.addPreference(preference)
        }
    }
}

class YABRSettings2UIComplexFragment : BaseFragment() {
    companion object {
        var showModule: UIComplex? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (showModule != null) {
            return showModule!!.onCreateUI(inflater.context)
        }
        @SuppressLint("SetTextI18n")
        return TextView(inflater.context).apply {
            text = "showModule is null"
        }
    }
}

