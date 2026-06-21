package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.getFieldValue
import io.github.duzhaokun123.yabr.utils.paramCount
import io.github.duzhaokun123.yabr.utils.toClass

@ModuleEntry(
    id = "pegasus_hook",
    targets = [ModuleEntryTarget.MAIN]
)
object PegasusHook: BaseModule(), Core, DexKitMemberOwner {
    typealias Interceptor = (Any) -> Unit
    private val interceptors = mutableListOf<Pair<String, Interceptor>>()

    val class_PegasusParser by dexKitMember(
        "com.bilibili.pegasus.request.PegasusParser",
    ) { bridge ->
        bridge.findClass {
            matcher {
                usingStrings("[Pegasus]PegasusParser")
            }
        }.single().toClass()
    }

    override fun onLoad(): Boolean {
        class_PegasusParser!!
            .findMethod { it.name == "convert" && it.paramCount == 1 && it.parameterTypes[0] == Object::class.java }
            .hookAfter {
                val data = it.result?.getFieldValue("data") ?: return@hookAfter
                interceptors.forEach { (id, interceptor) ->
                    runCatching {
                        interceptor(data)
                    }.onFailure { e ->
                        logger.e("interceptor($id) error: ", e)
                    }
                }
            }
        return true
    }

    fun addInterceptFirst(id: String, interceptor: Interceptor) {
        interceptors.add(0, id to interceptor)
    }

    fun addInterceptLast(id: String,interceptor: Interceptor) {
        interceptors.add(id to interceptor)
    }

    fun removeIntercept(id: String) {
        interceptors.removeIf { it.first == id }
    }
}