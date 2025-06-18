package io.github.duzhaokun123.yabr.utils

import io.github.duzhaokun123.yabr.module.core.JsonHelper

fun Any.getJsonFieldValue(key: String): Any? =
    JsonHelper.getJsonFieldValue(this, key)

fun Any.setJsonFieldValue(key: String, value: Any?) =
    JsonHelper.setJsonFieldValue(this, key, value)

@Suppress("UNCHECKED_CAST")
fun <T> Any.getJsonFieldValueAs(key: String): T =
    JsonHelper.getJsonFieldValue(this, key) as T

fun Any.getJsonFieldValueOrNull(key: String): Any? =
    runCatching { getJsonFieldValue(key) }.getOrNull()

@Suppress("UNCHECKED_CAST")
fun <T> Any.getJsonFieldValueOrNullAs(key: String): T =
    getJsonFieldValueOrNull(key) as T
