package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitContext
import io.github.duzhaokun123.yabr.utils.EarlyUtils
import io.github.duzhaokun123.yabr.utils.loaderContext
import org.luckypray.dexkit.DexKitBridge
import kotlin.reflect.KProperty

@ModuleEntry(
    id = "dexkit_helper",
    priority = 2,
)
object DexKitHelper : BaseModule(), Core {
    override val canUnload = false

    lateinit var dexKitBridge: DexKitBridge
        private set

    override fun onLoad(): Boolean {
        EarlyUtils.loadLibrary("dexkit")
        dexKitBridge = DexKitBridge.create(loaderContext.application.applicationInfo.sourceDir)
        Main.addOnModuleLoadListener { module ->
            if (module is DexKitContext) {
                runCatching {
                    module.onDexKitReady(dexKitBridge)
                }.onFailure { t ->
                    module.logger.e("Failed to initialize DexKit context: ${module.id}")
                    module.logger.e(t)
                }
                module.javaClass.declaredFields
                    .filter { it.type == DexKitMember::class.java }
                    .forEach {
                        it.isAccessible = true
                        val dexKitMember = it.get(module) as? DexKitMember<*> ?: return@forEach
                        runCatching {
                            dexKitMember.onBridgeReady(dexKitBridge)
                        }.onFailure { t ->
                            module.logger.e("Failed to initialize DexKit member: ${dexKitMember.name}")
                            module.logger.e(t)
                        }
                    }
            }
        }
        return true
    }
}

class DexKitMember<T>(
    val name: String,
    val cacheable: Boolean,
    val init: (DexKitBridge) -> T
) {
    var member: T? = null
        private set

    fun onCacheFound() {

    }

    fun onBridgeReady(bridge: DexKitBridge) {
        member = init(bridge)
    }

    operator fun getValue(t: Any?, property: KProperty<*>): T? {
        return member
    }
}
