package io.github.duzhaokun123.hooker.xposed100

import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.hooker.base.ImplementationInfo
import io.github.duzhaokun123.hooker.base.Unhooker
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

object Xposed100HookerContext : HookerContext {
    lateinit var xposedInterface: XposedInterface
    override val implementationInfo: ImplementationInfo
        get() = ImplementationInfo(
            name = "Xposed100",
            version = "${xposedInterface.frameworkVersion}(${xposedInterface.frameworkVersionCode})",
            description = "${xposedInterface.frameworkName} at privilege ${xposedInterface.frameworkPrivilege.toPrivilegeType()}"
        )

    fun init(xposedInterface: XposedInterface) {
        this.xposedInterface = xposedInterface
    }

    override fun hookMethod(
        method: Member, callback: HookCallback
    ): Unhooker {
        HookerKt.addCallback(method, callback)
        val unhooker = when (method) {
            is Method -> xposedInterface.hook(method, Hooker::class.java)
            is Constructor<*> -> xposedInterface.hook(method, Hooker::class.java)
            else -> throw IllegalArgumentException("Unsupported member type: ${method.javaClass.name}")
        }
        return {
            HookerKt.removeCallback(method, callback)
            unhooker.unhook()
        }
    }

    override fun invokeOriginal(
        method: Member, thiz: Any?, vararg args: Any?
    ): Any? {
        return when(method) {
            is Method -> xposedInterface.invokeOrigin(method, thiz, *args)
            is Constructor<*> ->
                @Suppress("UNCHECKED_CAST")
                xposedInterface.invokeOrigin(method as Constructor<Any>, thiz as Any, *args)
            else -> throw IllegalArgumentException("Unsupported member type: ${method.javaClass.name}")
        }
    }

    override fun <T> newInstanceOriginal(
        constructor: Constructor<T>,
        vararg args: Any?
    ): T {
        return xposedInterface.newInstanceOrigin(constructor, *args)
    }

    fun Int.toPrivilegeType(): String {
        return when (this) {
            XposedInterface.FRAMEWORK_PRIVILEGE_ROOT -> "FRAMEWORK_PRIVILEGE_ROOT($this)"
            XposedInterface.FRAMEWORK_PRIVILEGE_CONTAINER -> "FRAMEWORK_PRIVILEGE_CONTAINER($this)"
            XposedInterface.FRAMEWORK_PRIVILEGE_APP -> "FRAMEWORK_PRIVILEGE_APP($this)"
            XposedInterface.FRAMEWORK_PRIVILEGE_EMBEDDED -> "FRAMEWORK_PRIVILEGE_EMBEDDED($this)"
            else -> "UNKNOWN($this)"
        }
    }
}