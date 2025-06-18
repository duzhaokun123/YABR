package io.github.duzhaokun123.hooker.xposed100

import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Member

object HookerKt {
    private val callbacks = mutableListOf<Pair<Member, HookCallback>>()

    fun addCallback(member: Member, callback: HookCallback) {
        callbacks.add(member to callback)
    }

    fun removeCallback(member: Member, callback: HookCallback) {
        callbacks.remove(member to callback)
    }

    @JvmStatic
    fun before(callback: XposedInterface.BeforeHookCallback) {
        callbacks
            .filter { (member, _) -> member == callback.member }
            .forEach { (_, c) ->
                c.before(Xposed100HookBeforeCallbackContext(callback))
            }
    }

    @JvmStatic
    fun after(callback: XposedInterface.AfterHookCallback) {
        callbacks
            .filter { (member, _) -> member == callback.member }
            .forEach { (_, c) ->
                c.after(Xposed100HookAfterCallbackContext(callback))
            }
    }
}