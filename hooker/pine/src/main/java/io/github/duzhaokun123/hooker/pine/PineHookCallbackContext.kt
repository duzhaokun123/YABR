package io.github.duzhaokun123.hooker.pine

import io.github.duzhaokun123.hooker.base.HookCallbackContext
import top.canyie.pine.Pine
import java.lang.reflect.Member

class PineHookCallbackContext(
    val callFrame: Pine.CallFrame
): HookCallbackContext {
    override val method: Member
        get() = callFrame.method
    override val thiz: Any?
        get() = callFrame.thisObject
    override val args: Array<Any?>
        get() = callFrame.args
    override var result: Any?
        get() = callFrame.result
        set(value) {
            callFrame.result = value
        }
    override var throwable: Throwable?
        get() = callFrame.throwable
        set(value) {
            callFrame.throwable = value
        }

    override fun invokeOriginal(thiz: Any?, vararg args: Any?): Any? {
        return callFrame.invokeOriginalMethod(thiz, *args)
    }
}