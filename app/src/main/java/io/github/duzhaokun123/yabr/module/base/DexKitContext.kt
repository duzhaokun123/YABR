package io.github.duzhaokun123.yabr.module.base

import io.github.duzhaokun123.yabr.module.core.DexKitMember
import org.luckypray.dexkit.DexKitBridge

interface DexKitContext {
    fun onDexKitReady(bridge: DexKitBridge) {}
}

fun <T> DexKitContext.dexKitMember(
    name: String,
    cacheable: Boolean = true,
    init: (DexKitBridge) -> T
): DexKitMember<T> {
    val dexKitMember = DexKitMember(name, cacheable, init)
    return dexKitMember
}
