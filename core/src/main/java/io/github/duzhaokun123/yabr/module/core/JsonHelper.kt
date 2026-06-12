package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.module.base.lazyLoadClass
import io.github.duzhaokun123.yabr.utils.findFieldOrNull
import io.github.duzhaokun123.yabr.utils.invokeMethod
import io.github.duzhaokun123.yabr.utils.loadConstructor
import io.github.duzhaokun123.yabr.utils.toMethod
import java.lang.reflect.Field

@ModuleEntry(
    id = "json_helper"
)
object JsonHelper : BaseModule(), Core, DexKitMemberOwner {
    private val gson: Any by lazy { loadConstructor("Lcom/google/gson/Gson;-><init>()V").newInstance() }

    val class_JSONField by lazyLoadClass("com.alibaba.fastjson.annotation.JSONField")
    val class_SerializedName by lazyLoadClass("com.google.gson.annotations.SerializedName")

    val method_Gson_toJson by dexKitMember(
        "com.google.gson.Gson.toJson",
    ) { bridge ->
        bridge.findMethod {
            matcher {
                declaredClass("com.google.gson.Gson")
                returnType(String::class.java)
                paramTypes(Object::class.java)
            }
        }.single().toMethod()
    }

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
            val gsonAnnotation = it.getAnnotation(class_SerializedName as Class<Annotation>)
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

    fun gsonToString(any: Any): String? {
        return method_Gson_toJson!!.invoke(gson, any) as String?
    }
}