package io.github.duzhaokun123.yabr.utils

import java.lang.reflect.Constructor

fun Class<*>.findConstructorOrNull(filter: (Constructor<*>) -> Boolean): Constructor<*>? {
    return constructors.firstOrNull(filter)
}

fun Class<*>.findConstructor(filter: (Constructor<*>) -> Boolean): Constructor<*> {
    return findConstructorOrNull(filter) ?: throw NoSuchMethodException("No constructor found in ${this.name}")
}

val Constructor<*>.paramCount: Int
    get() = parameterTypes.size
