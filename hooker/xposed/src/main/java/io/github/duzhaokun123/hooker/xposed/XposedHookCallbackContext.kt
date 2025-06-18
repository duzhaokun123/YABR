package io.github.duzhaokun123.hooker.xposed

import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import io.github.duzhaokun123.hooker.base.HookCallbackContext
import java.lang.reflect.Member

class XposedHookCallbackContext(
    val param: MethodHookParam
) : HookCallbackContext {
    override val method: Member
        get() = param.method
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
            param.throwable = value
        }

    override fun invokeOriginal(thiz: Any?, vararg args: Any?): Any? {
        return XposedBridge.invokeOriginalMethod(param.method, thiz, args)
    }
}