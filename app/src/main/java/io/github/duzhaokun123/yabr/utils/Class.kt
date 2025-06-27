package io.github.duzhaokun123.yabr.utils

import com.ironz.unsafe.UnsafeAndroid
import org.luckypray.dexkit.result.ClassData
import org.luckypray.dexkit.wrap.DexClass

fun loadClass(name: String): Class<*> {
    return loaderContext.hostClassloader.loadClass(name)
}

fun loadClassOrNull(name: String): Class<*>? {
    return runCatching { loadClass(name) }.getOrNull()
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

fun Class<*>.unsafeNew(): Any {
    return Unsafe.instance.allocateInstance(this)
}
