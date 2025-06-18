package io.github.duzhaokun123.yabr.utils

import java.lang.reflect.Field

fun Class<*>.findFieldOrNull(findSuper: Boolean = true, filter: (Field) -> Boolean): Field? {
    declaredFields.forEach { field ->
        if (filter(field)) {
            return field
        }
    }
    if (findSuper) {
        superclass?.let { superClass ->
            return superClass.findFieldOrNull(true, filter)
        }
    }
    return null
}

fun Class<*>.findField(findSuper: Boolean = true, filter: (Field) -> Boolean): Field {
    return findFieldOrNull(findSuper, filter) ?: throw NoSuchFieldException("No field found in ${this.name}")
}

fun Any.getFieldValue(field: Field): Any? {
    field.isAccessible = true
    return field.get(this)
}

fun Any.getFieldValue(name: String, findSuper: Boolean = true): Any? {
    val field = this::class.java.findField(findSuper) { it.name == name }
    return getFieldValue(field)
}

fun Any.getFieldValueOrNull(name: String, findSuper: Boolean = true): Any? {
    return try {
        getFieldValue(name, findSuper)
    } catch (e: NoSuchFieldException) {
        null
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Any.getFieldValueAs(name: String, findSuper: Boolean = true): T {
    return getFieldValue(name, findSuper) as T
}

@Suppress("UNCHECKED_CAST")
fun <T> Any.getFieldValueOrNullAs(name: String, findSuper: Boolean = true): T? {
    return getFieldValueOrNull(name, findSuper) as T?
}

fun Any.setFieldValue(field: Field, value: Any?) {
    field.isAccessible = true
    field.set(this, value)
}

fun Any.setFieldValue(name: String, value: Any?, findSuper: Boolean = true) {
    val field = this::class.java.findField(findSuper) { it.name == name }
    setFieldValue(field, value)
}

fun Any.setFieldValueOrNull(name: String, value: Any?, findSuper: Boolean = true) {
    runCatching {
        setFieldValue(name, value, findSuper)
    }
}
