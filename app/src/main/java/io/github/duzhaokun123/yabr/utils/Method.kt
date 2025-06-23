package io.github.duzhaokun123.yabr.utils

import io.github.duzhaokun123.yabr.logger.AndroidLogger
import org.luckypray.dexkit.result.MethodData
import org.luckypray.dexkit.wrap.DexMethod
import java.lang.reflect.Method

fun Class<*>.findMethodOrNull(findSuper: Boolean = true, filter: (Method) -> Boolean): Method? {
    declaredMethods.forEach { method ->
        method.isAccessible = true
        if (filter(method)) {
            return method
        }
    }
    if (findSuper) {
        superclass?.let { superClass ->
            return superClass.findMethodOrNull(true, filter)
        }
    }
    return null
}

fun Class<*>.findMethod(findSuper: Boolean = true, filter: (Method) -> Boolean): Method {
    return findMethodOrNull(findSuper, filter)
        ?: throw NoSuchMethodException("No method found in ${this.name}")
}

fun Class<*>.findAllMethods(findSuper: Boolean = true, filter: (Method) -> Boolean): List<Method> {
    val methods = mutableListOf<Method>()
    declaredMethods.forEach { method ->
        method.isAccessible = true
        if (filter(method)) {
            methods.add(method)
        }
    }
    if (findSuper) {
        superclass?.let { superClass ->
            methods.addAll(superClass.findAllMethods(true, filter))
        }
    }
    return methods
}

fun Class<*>.findMethodBestMatch(
    name: String,
    vararg parameterTypes: Class<*>?,
    findSuper: Boolean = true
): Method {
    return findMethodOrNull(findSuper) { method ->
        if (method.name != name) return@findMethodOrNull false
        if (method.paramCount != parameterTypes.size) return@findMethodOrNull false
        return@findMethodOrNull parameterTypes
            .mapIndexed { index, clazz -> method.parameterTypes[index] to clazz }
            .all { (a, b) ->
                if (b == null) return@all true
                if (a == b) return@all true
                if (a.isAssignableFrom(b)) return@all true
                if (a.isPrimitive) {
                    return@all when (b) {
                        Boolean::class.javaObjectType -> a == Boolean::class.javaPrimitiveType
                        Byte::class.javaObjectType -> a == Byte::class.javaPrimitiveType
                        Char::class.javaObjectType -> a == Char::class.javaPrimitiveType
                        Short::class.javaObjectType -> a == Short::class.javaPrimitiveType
                        Int::class.javaObjectType -> a == Int::class.javaPrimitiveType
                        Long::class.javaObjectType -> a == Long::class.javaPrimitiveType
                        Float::class.javaObjectType -> a == Float::class.javaPrimitiveType
                        Double::class.javaObjectType -> a == Double::class.javaPrimitiveType
                        else -> false
                    }
                }
                return@all false
            }
    } ?: throw NoSuchMethodException(
        "No method found in ${this.name} with $name [${parameterTypes.joinToString(", ")}]"
    )
}

fun MethodData.toMethod(): Method {
    return this.getMethodInstance(loaderContext.hostClassloader)
}

fun DexMethod.toMethod(): Method {
    return this.getMethodInstance(loaderContext.hostClassloader)
}

val Method.paramCount: Int
    get() = parameterCount

fun Class<*>.getDeclaredMethodOrNull(
    name: String, vararg parameterTypes: Class<*>
): Method? {
    return runCatching { getDeclaredMethod(name, *parameterTypes) }.getOrNull()
}
