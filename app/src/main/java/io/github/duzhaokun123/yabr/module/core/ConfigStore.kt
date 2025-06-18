package io.github.duzhaokun123.yabr.module.core

import android.content.Context
import android.content.SharedPreferences
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.utils.loaderContext

@ModuleEntry(
    id = "config_store",
    priority = 1,
)
object ConfigStore : BaseModule(), Core {
    override val canUnload = false

    override fun onLoad(): Boolean {
        return true
    }

    val global by lazy {
        SharedPreferencesAccessor(
            loaderContext.application.getSharedPreferences(
                "yabr_config_global",
                Context.MODE_PRIVATE
            )
        )
    }

    fun ofModule(module: BaseModule): ConfigAccessor {
        return SharedPreferencesAccessor(
            loaderContext.application.getSharedPreferences(
                "yabr_config_${module.id}",
                Context.MODE_PRIVATE
            )
        )
    }
}

interface ConfigAccessor {
    fun getString(key: String, defaultValue: String? = null): String?
    fun getInt(key: String, defaultValue: Int? = null): Int?
    fun getBoolean(key: String, defaultValue: Boolean? = null): Boolean?
    fun getFloat(key: String, defaultValue: Float? = null): Float?
    fun getStringSet(key: String, defaultValue: Set<String>? = emptySet()): Set<String>?

    fun putString(key: String, value: String)
    fun putInt(key: String, value: Int)
    fun putBoolean(key: String, value: Boolean)
    fun putFloat(key: String, value: Float)
    fun putStringSet(key: String, value: Set<String>)

    fun remove(key: String)
}

class SharedPreferencesAccessor(
    val sharedPreferences: SharedPreferences
) : ConfigAccessor {
    override fun getString(key: String, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int?): Int? {
        return if (sharedPreferences.contains(key))
            sharedPreferences.getInt(key, defaultValue ?: 0)
        else
            defaultValue
    }

    override fun getBoolean(key: String, defaultValue: Boolean?): Boolean? {
        return if (sharedPreferences.contains(key))
            sharedPreferences.getBoolean(key, defaultValue ?: false)
        else
            defaultValue
    }

    override fun getFloat(key: String, defaultValue: Float?): Float? {
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getFloat(key, defaultValue ?: 0f)
        } else {
            defaultValue
        }
    }

    override fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? {
        return sharedPreferences.getStringSet(key, defaultValue)
    }

    override fun putString(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    override fun putInt(key: String, value: Int) {
        sharedPreferences.edit()
            .putInt(key, value)
            .apply()
    }

    override fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    override fun putFloat(key: String, value: Float) {
        sharedPreferences.edit()
            .putFloat(key, value)
            .apply()
    }

    override fun putStringSet(key: String, value: Set<String>) {
        sharedPreferences.edit()
            .putStringSet(key, value)
            .apply()
    }

    override fun remove(key: String) {
        sharedPreferences.edit()
            .remove(key)
            .apply()
    }
}
