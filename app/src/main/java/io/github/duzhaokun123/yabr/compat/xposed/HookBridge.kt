package io.github.duzhaokun123.yabr.compat.xposed

import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.duzhaokun123.yabr.BuildConfig
import io.github.duzhaokun123.yabr.utils.hookerContext
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

object HookBridge : IHookBridge {
    override fun getApiLevel() = 82
    override fun getFrameworkName() = "YABR"
    override fun getFrameworkVersion() = BuildConfig.VERSION_NAME
    override fun getFrameworkVersionCode() = BuildConfig.VERSION_CODE.toLong()

    override fun hookMethod(
        member: Member,
        callback: IHookBridge.IMemberHookCallback,
        priority: Int
    ): IHookBridge.MemberUnhookHandle {
        val unhooker = hookerContext.hookMethod(member, object : HookCallback{
            private var callbackExtra: Any? = null
            override fun before(callbackContext: HookCallbackContext) {
                callbackExtra = null
                callback.beforeHookedMember(object : IHookBridge.IMemberHookParam {
                    override fun getMember(): Member {
                        return member
                    }

                    override fun getThisObject(): Any? {
                        return callbackContext.thiz
                    }

                    override fun getArgs(): Array<out Any?> {
                        return callbackContext.args
                    }

                    override fun getResult(): Any? {
                        return callbackContext.result
                    }

                    override fun setResult(result: Any?) {
                        callbackContext.result = result
                    }

                    override fun getThrowable(): Throwable? {
                        return callbackContext.throwable
                    }

                    override fun setThrowable(throwable: Throwable) {
                        callbackContext.throwable = throwable
                    }

                    override fun getExtra(): Any? {
                        return callbackExtra
                    }

                    override fun setExtra(extra: Any?) {
                        callbackExtra = extra
                    }
                })
            }

            override fun after(callbackContext: HookCallbackContext) {
                callback.afterHookedMember(object : IHookBridge.IMemberHookParam {
                    override fun getMember(): Member {
                        return member
                    }

                    override fun getThisObject(): Any? {
                        return callbackContext.thiz
                    }

                    override fun getArgs(): Array<out Any?> {
                        return callbackContext.args
                    }

                    override fun getResult(): Any? {
                        return callbackContext.result
                    }

                    override fun setResult(result: Any?) {
                        callbackContext.result = result
                    }

                    override fun getThrowable(): Throwable? {
                        return callbackContext.throwable
                    }

                    override fun setThrowable(throwable: Throwable) {
                        callbackContext.throwable = throwable
                    }

                    override fun getExtra(): Any? {
                        return callbackExtra
                    }

                    override fun setExtra(extra: Any?) {
                        callbackExtra = extra
                    }

                })
            }
        })
        return object : IHookBridge.MemberUnhookHandle {
            private var isHookActive = true
            override fun getMember(): Member {
                return member
            }

            override fun getCallback(): IHookBridge.IMemberHookCallback {
                return callback
            }

            override fun isHookActive(): Boolean {
                return isHookActive
            }

            override fun unhook() {
                unhooker.invoke()
                isHookActive = false
            }

        }
    }

    override fun isDeoptimizationSupported(): Boolean {
        return false
    }

    override fun deoptimize(member: Member): Boolean {
        return false
    }

    override fun invokeOriginalMethod(
        method: Method,
        thisObject: Any?,
        args: Array<out Any?>
    ): Any? {
        return hookerContext.invokeOriginal(method, thisObject, *args)
    }

    override fun <T : Any?> invokeOriginalConstructor(
        ctor: Constructor<T?>,
        thisObject: T & Any,
        args: Array<out Any?>
    ) {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> newInstanceOrigin(
        constructor: Constructor<T?>,
        vararg args: Any?
    ): T & Any {
        @Suppress("UNCHECKED_CAST")
        return hookerContext.invokeOriginal(constructor, null, *args) as (T & Any)
    }

    override fun getHookCounter(): Long {
        return -1
    }

    override fun getHookedMethods(): Set<Member?>? {
        return null
    }
}