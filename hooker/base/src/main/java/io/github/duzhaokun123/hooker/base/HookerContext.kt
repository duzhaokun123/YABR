package io.github.duzhaokun123.hooker.base

import java.lang.reflect.Member

typealias Unhooker = () -> Unit

interface HookCallbackContext {
    val method: Member
    val thiz: Any?
    val args: Array<Any?>
    var result: Any?
    var throwable: Throwable?
    fun invokeOriginal(thiz: Any?, vararg args: Any?): Any?
}

interface HookCallback {
    fun before(callbackContext: HookCallbackContext) {}
    fun after(callbackContext: HookCallbackContext) {}
}

interface HookerContext {
    val implementationInfo: ImplementationInfo

    fun hookMethod(method: Member, callback: HookCallback): Unhooker
    fun invokeOriginal(method: Member, thiz: Any?, vararg args: Any?): Any?
}

val HookCallbackContext.thisObject: Any?
    get() = thiz
