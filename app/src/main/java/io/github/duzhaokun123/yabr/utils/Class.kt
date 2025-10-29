package io.github.duzhaokun123.yabr.utils

import org.luckypray.dexkit.result.ClassData
import org.luckypray.dexkit.wrap.DexClass

fun loadClass(signature: String): Class<*> {
    runCatching {
        return DexClass.deserialize(signature).toClass()
    }.onFailure {
        return loaderContext.hostClassloader.loadClass(signature)
    }.getOrThrow()
}

fun loadClassOrNull(signature: String): Class<*>? {
    return runCatching { loadClass(signature) }.getOrNull()
}

fun ClassData.toClass(): Class<*> {
    return getInstance(loaderContext.hostClassloader)
}

fun Class<*>.new(vararg args: Any?): Any {
    return findConstructor { it.paramCount == args.size }.newInstance(*args)
}

fun Class<*>.newOrNull(vararg args: Any?): Any? {
    return runCatching { new(*args) }.getOrNull()
}

@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.newAs(vararg args: Any?): T {
    return new(*args) as T
}

fun DexClass.toClass(): Class<*> {
    return this.getInstance(loaderContext.hostClassloader)
}

fun Class<*>.allocateInstance(): Any {
    return Unsafe.instance.allocateInstance(this)
}
