package io.github.duzhaokun123.yabr.utils

import org.luckypray.dexkit.result.MethodData
import java.lang.reflect.Method

fun MethodData.invoke(obj: Any?, vararg args: Any?): Any? {
    return if (this.isMethod) {
        this.getMethodInstance(loaderContext.hostClassloader).invoke(obj, *args)
    } else if (this.isConstructor) {
        this.getConstructorInstance(loaderContext.hostClassloader).newInstance(*args)
    } else {
        throw IllegalArgumentException("MethodData $this is neither a method nor a constructor")
    }
}

fun Any.invokeMethod(
    name: String,
    vararg args: Any?
): Any? {
    val method = this.javaClass.findMethodBestMatch(name, *args.map { it?.javaClass }.toTypedArray(), findSuper = true)
    return method.invoke(this, *args)
}

fun Any.invokeMethod(
    method: Method,
    vararg args: Any?
): Any? {
    return method.invoke(this, *args)
}

@Suppress("UNCHECKED_CAST")
fun <T> Any.invokeMethodAs(
    name: String,
    vararg args: Any?,
): T {
    return invokeMethod(name, *args) as T
}

@Suppress("UNCHECKED_CAST")
fun <T> Any.invokeMethodAs(
    method: Method,
    vararg args: Any?,
): T {
    return invokeMethod(method, *args) as T
}

fun Class<*>.invokeStaticMethod(
    name: String,
    vararg args: Any?
): Any? {
    val method = this.findMethodBestMatch(name, *args.map { it?.javaClass }.toTypedArray(), findSuper = true)
    return method.invoke(null, *args)
}

@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.invokeStaticMethodAs(
    name: String,
    vararg args: Any?,
): T {
    return invokeStaticMethod(name, *args) as T
}
