package io.github.duzhaokun123.hooker.xposed100

import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Member

class Xposed100HookBeforeCallbackContext(
    val callback: XposedInterface.BeforeHookCallback
) : HookCallbackContext {
    private var _result: Any? = null
    private var _throwable: Throwable? = null

    override val method: Member
        get() = callback.member
    override val thiz: Any?
        get() = callback.thisObject
    override val args: Array<Any?>
        get() = callback.args
    override var result: Any?
        get() = _result
        set(value) {
            _result = value
            callback.returnAndSkip(value)
        }
    override var throwable: Throwable?
        get() = _throwable
        set(value) {
            _throwable = value
            callback.throwAndSkip(value)
        }

    override fun invokeOriginal(thiz: Any?, vararg args: Any?): Any? {
        return Xposed100HookerContext.invokeOriginal(method, thiz, *args)
    }
}

class Xposed100HookAfterCallbackContext(
    val callback: XposedInterface.AfterHookCallback
) : HookCallbackContext {
    override val method: Member
        get() = callback.member
    override val thiz: Any?
        get() = callback.thisObject
    override val args: Array<Any?>
        get() = callback.args
    override var result: Any?
        get() = callback.result
        set(value) {
            callback.result = value
        }
    override var throwable: Throwable?
        get() = callback.throwable
        set(value) {
            callback.throwable = value
        }

    override fun invokeOriginal(thiz: Any?, vararg args: Any?): Any? {
        return Xposed100HookerContext.invokeOriginal(method, thiz, *args)
    }
}
