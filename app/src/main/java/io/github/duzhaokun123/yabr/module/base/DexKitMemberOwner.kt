package io.github.duzhaokun123.yabr.module.base

import io.github.duzhaokun123.yabr.module.core.DexKitMember
import org.luckypray.dexkit.DexKitBridge

interface DexKitMemberOwner {
    val needDexKitBridge: Boolean
        get() = false
    fun onDexKitReady(bridge: DexKitBridge) {}
}

fun <T> DexKitMemberOwner.dexKitMember(
    name: String,
    cacheable: Boolean = true,
    init: (DexKitBridge) -> T
): DexKitMember<T> {
    val dexKitMember = DexKitMember(name, cacheable, init)
    return dexKitMember
}
