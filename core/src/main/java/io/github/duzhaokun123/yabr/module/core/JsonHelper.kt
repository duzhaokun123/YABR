package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.logger.AndroidLogger
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.lazyLoadClass
import io.github.duzhaokun123.yabr.utils.findField
import io.github.duzhaokun123.yabr.utils.findFieldOrNull
import io.github.duzhaokun123.yabr.utils.invokeMethod
import java.lang.reflect.Field

@ModuleEntry(
    id = "json_helper"
)
object JsonHelper : BaseModule(), Core {
    val class_JSONField by lazyLoadClass("com.alibaba.fastjson.annotation.JSONField")
    val class_SerializedName by lazyLoadClass("com.google.gson.annotations.SerializedName")

    override fun onLoad(): Boolean {
        return true
    }

    fun getJsonField(data: Any, key: String): Field {
        val field = data.javaClass.findFieldOrNull {
            @Suppress("UNCHECKED_CAST")
            val fastjsonAnnotation = it.getAnnotation(class_JSONField as Class<Annotation>)
            if (fastjsonAnnotation != null) {
                return@findFieldOrNull fastjsonAnnotation.invokeMethod("name") == key
            }
            val gsonAnnotation = it.annotations.find {
                it.toString().startsWith("@com.google.gson.annotations.SerializedName(")
            }
            if (gsonAnnotation != null) {
                return@findFieldOrNull gsonAnnotation.invokeMethod("value") == key
            }
            return@findFieldOrNull false
        } ?: throw NoSuchFieldException("No field found for key: $key in ${data.javaClass.name} or its superclasses")
        field.isAccessible = true
        return field
    }

    fun getJsonFieldValue(data: Any, key: String): Any? {
        val field = getJsonField(data, key)
        return field.get(data)
    }

    fun setJsonFieldValue(data: Any, key: String, value: Any?) {
        val field = getJsonField(data, key)
        field.set(data, value)
    }
}