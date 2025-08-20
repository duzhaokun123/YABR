package io.github.duzhaokun123.hooker.qauxvapi

import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.qauxv.chainloader.api.ChainLoaderAgent
import io.github.qauxv.loader.hookapi.IHookBridge
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

class QAuxvHookCallbackContext(
    val param: IHookBridge.IMemberHookParam
) : HookCallbackContext {
    override val method: Member
        get() = param.member
    override val thiz: Any?
        get() = param.thisObject
    override val args: Array<Any?>
        get() = param.args
    override var result: Any?
        get() = param.result
        set(value) {
            param.result = value
        }
    override var throwable: Throwable?
        get() = param.throwable
        set(value) {
            if (value != null) {
                param.setThrowable(value)
            }
        }

    override fun invokeOriginal(thiz: Any?, vararg args: Any?): Any? {
        val hookBridge = ChainLoaderAgent.getHookBridge()
        val method = param.member
        return when (method) {
            is Method -> hookBridge.invokeOriginalMethod(method, thiz, args)
            is Constructor<*> -> hookBridge.newInstanceOrigin(method, *args)
            else -> throw RuntimeException("Unsupported member type: ${method.javaClass.name}")
        }
    }
}