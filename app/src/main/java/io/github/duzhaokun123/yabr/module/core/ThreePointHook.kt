package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitContext
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.module.base.lazyLoadClass
import io.github.duzhaokun123.yabr.module.core.JsonHelper.getJsonFieldValue
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.findMethodOrNull
import io.github.duzhaokun123.yabr.utils.getFieldValue
import io.github.duzhaokun123.yabr.utils.getFieldValueAs
import io.github.duzhaokun123.yabr.utils.getJsonFieldValue
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueOrNullAs
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loadClassOrNull
import io.github.duzhaokun123.yabr.utils.new
import io.github.duzhaokun123.yabr.utils.paramCount
import io.github.duzhaokun123.yabr.utils.setFieldValue
import io.github.duzhaokun123.yabr.utils.setJsonFieldValue
import io.github.duzhaokun123.yabr.utils.toClass
import io.github.duzhaokun123.yabr.utils.toMethod
import kotlin.math.PI
import kotlin.random.Random

data class ThreePointItemItemData(
    val name: String,
    val data: String?
)

interface ThreePointCallback {
    fun parseData(data: Any): ThreePointItemItemData?
    fun onClick(data: ThreePointItemItemData)
}

@ModuleEntry(
    id = "three_point_hook",
    targets = [ModuleEntryTarget.MAIN]
)
object ThreePointHook : BaseModule(), Core, DexKitContext {
    val threePointCallbackMap =
        mutableMapOf<Long, ThreePointCallback>()

    val class_PegasusParser by dexKitMember(
        "com.bilibili.pegasus.request.PegasusParser",
    ) { bridge ->
        bridge.findClass {
            matcher {
                usingStrings("[Pegasus]PegasusParser")
            }
        }.single().toClass()
    }
    val class_TMIndexApiParser by dexKitMember(
        "com.bilibili.pegasus.api.TMIndexApiParser",
    ) { bridge ->
        bridge.findClass {
            matcher {
                usingStrings("TMIndexApiParser", "card_type is empty")
            }
        }.single().toClass()
    }
    val class_ThreePointItem by lazyLoadClass("com.bilibili.app.comm.list.common.data.ThreePointItem")
    val class_DislikeReason by lazyLoadClass("com.bilibili.app.comm.list.common.data.DislikeReason")
    val class_CardClickProcessor by dexKitMember(
        "com.bilibili.pegasus.card.base.CardClickProcessor",
    ) { bridge ->
        bridge.findClass {
            matcher {
                usingStrings("handleWatchLaterClicked, createType = ")
            }
        }.single().toClass()
    }

    override fun onLoad(): Boolean {
        class_PegasusParser
            ?.findMethod { it.name == "convert" && it.paramCount == 1 && it.parameterTypes[0] == Object::class.java }
            ?.hookAfter {
                val data = it.result?.getFieldValue("data") ?: return@hookAfter
                hookPegasusFeedConvert(data)
            }
        class_TMIndexApiParser
            ?.findMethod { it.name == "convert" && it.paramCount == 1 && it.parameterTypes[0] == Object::class.java }
            ?.hookAfter {
                val data = it.result?.getFieldValue("data") ?: return@hookAfter
                hookPegasusFeedConvert(data)
            }
        class_CardClickProcessor
            ?.findMethod(findSuper = false) { it.paramCount == 9 }
            ?.hookBefore {
                if (hookDislikeReason(it.args[3])) {
                    it.result = null
                }
            }
        loadClassOrNull("com.bilibili.pegasus.ext.threepoint.ThreePointKt")
            ?.findMethodOrNull(findSuper = false) { it.paramCount == 8 }
            ?.hookBefore {
                if (hookDislikeReason(it.args[3])) {
                    it.result = null
                }
            }

        return true
    }

    fun addThreePointCallback(
        id: Long, callback: ThreePointCallback
    ) {
        threePointCallbackMap.put(id, callback)
    }

    fun removeThreePointCallback(id: Long) {
        threePointCallbackMap.remove(id)
    }

    private fun hookPegasusFeedConvert(data: Any) {
        data.getJsonFieldValueAs<ArrayList<Any>>("items").forEach { item ->
            val threePoint = item.getJsonFieldValueAs<MutableList<Any>?>("three_point_v2")
            val reasons = mutableListOf<Any>()
            val threePointItem = class_ThreePointItem.new()
            threePointItem.setFieldValue("title", "YABR")
            threePointItem.setFieldValue("subtitle", "  menu")
            threePointItem.setFieldValue("type", "dislike")
            threePointItem.setFieldValue("reasons", reasons)
            threePointCallbackMap.forEach { (id, callback) ->
                val data = runCatching { callback.parseData(item) }
                    .onFailure { t ->
                        logger.w("parse pegasus feed callback $id failed")
                        logger.w(t)
                    }.getOrNull()
                    ?: return@forEach
                val dislikeReason = class_DislikeReason.new()
                dislikeReason.setFieldValue("id", id)
                dislikeReason.setFieldValue("name", data.name)
                dislikeReason.setFieldValue("extra", data.data)
                reasons.add(dislikeReason)
            }
            if (reasons.isEmpty()) return
            if (threePoint != null) {
                threePoint.add(threePointItem)
            } else {
                item.setJsonFieldValue("three_point_v2", mutableListOf(threePointItem))
            }
        }
    }

    private fun hookDislikeReason(reason: Any?): Boolean {
        val id = reason?.getFieldValue("id") ?: return false
        val callback = threePointCallbackMap[id] ?: return false
        val name = reason.getFieldValueAs<String>("name")
        val data = reason.getJsonFieldValueAs<String?>("extra")
        val threePointItemItemData = ThreePointItemItemData(name, data)
        runCatching {
            callback.onClick(threePointItemItemData)
        }.onFailure { t ->
            logger.w("ThreePointCallback $id onClick failed")
            logger.w(t)
        }
        return true
    }
}