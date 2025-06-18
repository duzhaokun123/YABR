package io.github.duzhaokun123.hooker.pine

import android.annotation.SuppressLint
import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.hooker.base.ImplementationInfo
import io.github.duzhaokun123.hooker.base.Unhooker
import io.github.duzhaokun123.yabr.utils.EarlyUtils
import top.canyie.pine.Pine
import top.canyie.pine.PineConfig
import top.canyie.pine.callback.MethodHook
import java.lang.reflect.Member

@SuppressLint("UnsafeDynamicallyLoadedCode")
class PineHookerContext : HookerContext {
    init {
        PineConfig.debug = BuildConfig.DEBUG
        PineConfig.debuggable = BuildConfig.DEBUG
        EarlyUtils.loadLibrary("pine")
    }

    override val implementationInfo: ImplementationInfo
        get() = ImplementationInfo(
            name = "Pine",
            version = "0.3.0",
        )

    override fun hookMethod(
        method: Member,
        callback: HookCallback
    ): Unhooker {
        val unhook =  Pine.hook(method, object : MethodHook() {
            override fun beforeCall(callFrame: Pine.CallFrame) {
                callback.before(PineHookCallbackContext(callFrame))
            }

            override fun afterCall(callFrame: Pine.CallFrame) {
                callback.after(PineHookCallbackContext(callFrame))
            }
        })
        return {
            unhook.unhook()
        }
    }

    override fun invokeOriginal(
        method: Member, thiz: Any?, vararg args: Any?
    ): Any? {
        return Pine.invokeOriginalMethod(method, thiz, *args)
    }
}