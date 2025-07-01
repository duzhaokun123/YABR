package dev.o0kam1.settingsui2

import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.bilibili.lib.ui.BasePreferenceFragment
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.SettingsUI
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.invokeMethod
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loaderContext
import java.lang.reflect.Proxy

@ModuleEntry(
    id = "dev.o0kam1.ui.SettingsUI2",
    targets = [ModuleEntryTarget.MAIN]
)
object SettingsUI2 : BaseModule() {
    override val canUnload = false

    lateinit var class_OnPreferenceClickListener: Class<*>

    override fun onLoad(): Boolean {
        class_OnPreferenceClickListener =
            loadClass("androidx.preference.Preference")
                .findMethod { it.name == "setOnPreferenceClickListener" }
                .parameterTypes[0]
        loadClass("com.bilibili.app.preferences.BiliPreferencesActivity\$BiliPreferencesFragment")
            .findMethod { it.name == "onCreatePreferences" }
            .hookAfter {
                val preferenceFragment = it.thiz as BasePreferenceFragment
                val context = preferenceFragment.requireContext()
                val preferenceCategoryYABR = PreferenceCategory(context).apply {
                    title = "YABR"
                }
                preferenceFragment.preferenceScreen.addPreference(preferenceCategoryYABR)
                val preferenceYABRSettings = Preference(context).apply {
                    title = "YABR 设置 v1"
                    invokeMethod("setOnPreferenceClickListener", OnPreferenceClickListener {
                        SettingsUI.showSettings(context)
                        true
                    })
                }
                preferenceCategoryYABR.addPreference(preferenceYABRSettings)
            }
        return true
    }

    fun OnPreferenceClickListener(callback: (Preference) -> Boolean): Any {
        return Proxy.newProxyInstance(
            loaderContext.hostClassloader,
            arrayOf(class_OnPreferenceClickListener)
        ) { _, method, args ->
             return@newProxyInstance when (method.name) {
                "onPreferenceClick" -> callback(args[0] as Preference)
                 else -> Unit
            }
        }
    }
}


