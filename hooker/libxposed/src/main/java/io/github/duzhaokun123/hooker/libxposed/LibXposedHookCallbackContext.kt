package io.github.duzhaokun123.hooker.libxposed

import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Member
import java.lang.reflect.Constructor

class LibXposedHookCallbackContext(
    private val chain: XposedInterface.Chain,
    private val invoker: XposedInterface.Invoker<*, *>
) : HookCallbackContext {
    private var resultValue: Any? = null
    private var throwableValue: Throwable? = null
    private var resultSet = false
    private var throwableSet = false
    private val argsValue: Array<Any?> = chain.args.toTypedArray()

    override val method: Member
        get() = chain.executable as Member
    override val thiz: Any?
        get() = chain.thisObject
    override val args: Array<Any?>
        get() = argsValue
    override var result: Any?
        get() = resultValue
        set(value) {
            resultValue = value
            resultSet = true
        }
    override var throwable: Throwable?
        get() = throwableValue
        set(value) {
            throwableValue = value
            throwableSet = true
        }

    @Suppress("UNCHECKED_CAST")
    override fun invokeOriginal(thiz: Any?, vararg args: Any?): Any? {
        return when (chain.executable) {
            is Constructor<*> -> (invoker as XposedInterface.CtorInvoker<Any>).newInstance(*args)
            else -> invoker.invoke(thiz, *args)
        }
    }

    fun shouldSkipOriginal(): Boolean {
        return resultSet || throwableSet
    }

    fun throwIfNeeded() {
        val error = throwableValue
        if (throwableSet && error != null) {
            throw error
        }
    }
}
