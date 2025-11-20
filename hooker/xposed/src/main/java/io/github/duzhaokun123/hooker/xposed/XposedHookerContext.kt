package io.github.duzhaokun123.hooker.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.hooker.base.ImplementationInfo
import io.github.duzhaokun123.hooker.base.Unhooker
import java.lang.reflect.Constructor
import java.lang.reflect.Member

class XposedHookerContext(
    val lpparam: XC_LoadPackage.LoadPackageParam
) : HookerContext {
    val xposedTag = XposedBridge::class.java.getField("TAG").get(null)

    override val implementationInfo: ImplementationInfo
        get() = ImplementationInfo(
            name = "XposedLegacy",
            version = "${XposedBridge.getXposedVersion()}",
            description = "Xposed Framework, with log TAG $xposedTag"
        )

    override fun hookMethod(
        method: Member,
        callback: HookCallback
    ): Unhooker {
        val xpUnhook = XposedBridge.hookMethod(method, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                callback.before(XposedHookCallbackContext(param))
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                callback.after(XposedHookCallbackContext(param))
            }
        })
        return {
            xpUnhook.unhook()
        }
    }

    override fun invokeOriginal(
        method: Member, thiz: Any?, vararg args: Any?
    ): Any? {
        return XposedBridge.invokeOriginalMethod(method, thiz, args)
    }

    override fun <T> newInstanceOriginal(
        constructor: Constructor<T>,
        vararg args: Any?
    ): T {
        @Suppress("UNCHECKED_CAST")
        return XposedBridge.invokeOriginalMethod(constructor, null, args) as T
    }
}