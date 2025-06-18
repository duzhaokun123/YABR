package io.github.duzhaokun123.hooker.noop

import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.hooker.base.ImplementationInfo
import io.github.duzhaokun123.hooker.base.Unhooker
import java.lang.reflect.Member

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

}