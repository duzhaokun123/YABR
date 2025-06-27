package io.github.duzhaokun123.hooker.noop

import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.hooker.base.ImplementationInfo
import io.github.duzhaokun123.hooker.base.Unhooker
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.security.InvalidParameterException

object NoOpHookerContext : HookerContext {
    override val implementationInfo: ImplementationInfo
        get() = ImplementationInfo(
            name = "NoOp",
            version = "-",
            description = "do nothing"
        )

    override fun hookMethod(
        method: Member,
        callback: HookCallback
    ): Unhooker {
        return {
            // No operation, just return a no-op unhooker
        }
    }

    override fun invokeOriginal(
        method: Member,
        thiz: Any?,
        vararg args: Any?
    ): Any? {
        return when (method) {
            is Method -> method.invoke(thiz, *args)
            is Constructor<*> -> method.newInstance(*args)
            else -> InvalidParameterException("method $method")
        }
    }
}