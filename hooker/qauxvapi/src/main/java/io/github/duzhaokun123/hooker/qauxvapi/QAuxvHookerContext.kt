package io.github.duzhaokun123.hooker.qauxvapi

import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.hooker.base.ImplementationInfo
import io.github.duzhaokun123.hooker.base.Unhooker
import io.github.qauxv.chainloader.api.ChainLoaderAgent
import io.github.qauxv.loader.hookapi.IHookBridge
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

class QAuxvHookerContext : HookerContext {
    val hookBridge = ChainLoaderAgent.getHookBridge()

    override val implementationInfo: ImplementationInfo
        get() = ImplementationInfo(
            name = "QAuxvAPI",
            version = "${hookBridge.apiLevel}",
            description = "QAuxvAPI Hooker\n" +
                    "framework: ${hookBridge.frameworkName} ${hookBridge.frameworkVersion} (${hookBridge.frameworkVersionCode})"
        )

    override fun hookMethod(
        method: Member,
        callback: HookCallback
    ): Unhooker {
        val unhookHandle = hookBridge.hookMethod(method, object : IHookBridge.IMemberHookCallback {
            override fun beforeHookedMember(param: IHookBridge.IMemberHookParam) {
                callback.before(QAuxvHookCallbackContext(param))
            }

            override fun afterHookedMember(param: IHookBridge.IMemberHookParam) {
                callback.after(QAuxvHookCallbackContext(param))
            }
        }, IHookBridge.PRIORITY_DEFAULT)
        return {
            unhookHandle.unhook()
        }
    }

    override fun invokeOriginal(
        method: Member,
        thiz: Any?,
        vararg args: Any?
    ): Any? {
        return when (method) {
            is Method -> hookBridge.invokeOriginalMethod(method, thiz, args)
            is Constructor<*> ->
                @Suppress("UNCHECKED_CAST")
                hookBridge.invokeOriginalConstructor(method as Constructor<Any>, thiz!!, args)
            else -> throw RuntimeException("Unsupported member type: ${method.javaClass.name}")
        }
    }

    override fun <T> newInstanceOriginal(
        constructor: Constructor<T>,
        vararg args: Any?
    ): T {
        return hookBridge.newInstanceOrigin(constructor, *args)
    }
}