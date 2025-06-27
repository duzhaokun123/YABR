package io.github.duzhaokun123.yabr.module.core

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitContext
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.module.base.lazyLoadClass
import io.github.duzhaokun123.yabr.module.base.multiLoadAnySuccess
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.getFieldValue
import io.github.duzhaokun123.yabr.utils.getFieldValueAs
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.new
import io.github.duzhaokun123.yabr.utils.paramCount
import io.github.duzhaokun123.yabr.utils.setFieldValue
import io.github.duzhaokun123.yabr.utils.setJsonFieldValue
import io.github.duzhaokun123.yabr.utils.toClass
import io.github.duzhaokun123.yabr.utils.toMethod
import io.github.duzhaokun123.yabr.utils.unsafeNew
import java.lang.reflect.Method
import java.lang.reflect.Proxy

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

    override fun onLoad() =
        multiLoadAnySuccess(::hookPegasus1, ::hookPegasus2, ::hookTheseus)

    fun hookPegasus1(): Boolean {
        class_PegasusParser!!
            .findMethod { it.name == "convert" && it.paramCount == 1 && it.parameterTypes[0] == Object::class.java }
            .hookAfter {
                val data = it.result?.getFieldValue("data") ?: return@hookAfter
                hookPegasusFeedConvert(data)
            }
        loadClass("com.bilibili.pegasus.ext.threepoint.ThreePointKt")
            .findMethod(findSuper = false) { it.paramCount >= 4 && it.parameterTypes[3] == class_DislikeReason }
            .hookBefore {
                if (hookPegasusDislikeReason(it.args[3])) {
                    it.result = null
                }
            }
        return true
    }

    fun hookPegasus2(): Boolean {
        class_TMIndexApiParser!!
            .findMethod { it.name == "convert" && it.paramCount == 1 && it.parameterTypes[0] == Object::class.java }
            .hookAfter {
                val data = it.result?.getFieldValue("data") ?: return@hookAfter
                hookPegasusFeedConvert(data)
            }
        class_CardClickProcessor!!
            .findMethod(findSuper = false) { it.paramCount == 9 }
            .hookBefore {
                if (hookPegasusDislikeReason(it.args[3])) {
                    it.result = null
                }
            }
        return true
    }

    val class_DetailRelateService by lazyLoadClass("com.bilibili.ship.theseus.united.page.intro.module.relate.DetailRelateService")
    val method_DetailRelateService_onClickMore by dexKitMember(
        "com.bilibili.ship.theseus.united.page.intro.module.relate.DetailRelateService.onClickMore"
    ) { bridge ->
        bridge.findMethod {
            matcher {
                declaredClass(class_DetailRelateService)
                usingStrings("DetailRelateService", "onClickMore, threePoint is null")
            }
        }.single().toMethod()
    }
    val method_DetailRelateService_addThreePointData by dexKitMember(
        "com.bilibili.ship.theseus.united.page.intro.module.relate.DetailRelateService.addThreePointData"
    ) { bridge ->
        bridge.findMethod {
            matcher {
                declaredClass(class_DetailRelateService)
                addCaller {
                    usingStrings("DetailRelateService", "onClickMore, threePoint is null")
                }
            }
        }.single().toMethod()
    }
    val class_RelateDislike by dexKitMember(
        "com.bilibili.ship.theseus.united.page.intro.module.relate.RelateDislike"
    ) { bridge ->
        bridge.findClass {
            searchPackages("com.bilibili.ship.theseus.united.page.intro.module.relate")
            matcher {
                usingStrings("RelateDislike(title=")
            }
        }.single().toClass()
    }
    val class_RelateReasons by dexKitMember(
        "com.bilibili.ship.theseus.united.page.intro.module.relate.RelateReasons"
    ) { bridge ->
        bridge.findClass {
            searchPackages("com.bilibili.ship.theseus.united.page.intro.module.relate")
            matcher {
                usingStrings("RelateReasons(id=")
            }
        }.single().toClass()
    }

    fun hookTheseus(): Boolean {
        method_DetailRelateService_onClickMore!!
            .hookBefore {
                val relateCard: Any
                val z0: Boolean
                val function0: Any
                val version: Int
                when(it.args.size) {
                    3 -> { // 国内 7.71.0
                        relateCard = it.args[0] as Any
                        z0 = it.args[1] as Boolean
                        function0 = Unit
                        version = 0
                    }
                    6 -> { // 国内 8.51.0
                        relateCard = it.args[1] as Any
                        z0 = it.args[2] as Boolean
                        function0 = it.args[3] as Any
                        version = 1
                    }
                    else -> {
                        logger.e("unsupported method_DetailRelateService_onClickMore\n\t${it.method}")
                        return@hookBefore
                    }
                }
                val relateDislike = class_RelateDislike!!.unsafeNew()
                relateDislike.setFieldValue("a", "YABR") // title
                relateDislike.setFieldValue("b", " menu") // subtitle
                relateDislike.setFieldValue("c", " menu") // closedSubtitle
                val dislikeReasons = mutableListOf<Any>()
                val datas = parseData(relateCard)
                datas.forEach { (id, data) ->
                    val relateReason = class_RelateReasons!!.unsafeNew()
                    relateReason.setFieldValue("a", id) // id
                    relateReason.setFieldValue("e", data.name) // name
                    dislikeReasons.add(relateReason)
                }
                if (dislikeReasons.isEmpty()) return@hookBefore
                if (dislikeReasons.size == 1) {
                    val relateReason = class_RelateReasons!!.unsafeNew()
                    relateReason.setFieldValue("e", "placeholder") // name
                    dislikeReasons.add(relateReason)
                }
                relateDislike.setFieldValue("f", dislikeReasons) // dislikeReason
                val callback = Proxy.newProxyInstance(
                    loaderContext.hostClassloader,
                    arrayOf(loadClass("kotlin.jvm.functions.Function1"))
                ) { _, method, args ->
                    val cancelDislikeData = args[0]
                    val feedbackId = cancelDislikeData.getFieldValueAs<String?>("d")?.toLongOrNull() ?: return@newProxyInstance Unit
                    val data = datas.find { it.first == feedbackId }?.second ?: return@newProxyInstance Unit
                    callCallback(feedbackId, data)
                }
                when(version) {
                    0 -> {
                        // FIXME: not added
                        method_DetailRelateService_addThreePointData!!
                            .invoke(it.thiz, relateCard, z0, arrayListOf<Any>(), relateDislike, true, callback)
                    }
                    1 -> method_DetailRelateService_addThreePointData!!
                        .invoke(it.thiz, relateCard, z0, relateDislike, true, function0, callback)
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
            parseData(item).forEach { (id, data) ->
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

    private fun hookPegasusDislikeReason(reason: Any?): Boolean {
        val id = reason?.getFieldValueAs<Long?>("id") ?: return false
        val name = reason.getFieldValueAs<String>("name")
        val data = reason.getJsonFieldValueAs<String?>("extra")
        val threePointItemItemData = ThreePointItemItemData(name, data)
        callCallback(id, threePointItemItemData)
        return true
    }

    private fun parseData(data: Any): List<Pair<Long, ThreePointItemItemData>> {
        return threePointCallbackMap.mapNotNull { (id, callback) ->
            runCatching {
                callback.parseData(data)
            }.onFailure { t ->
                logger.w("parse pegasus feed callback $id failed")
                logger.w(t)
            }.getOrNull()
                ?.let { id to it }
        }
    }

    private fun callCallback(id: Long, data: ThreePointItemItemData) {
        runCatching {
            threePointCallbackMap[id]?.onClick(data)
        }.onFailure { t ->
            logger.w("ThreePointCallback $id onClick failed")
            logger.w(t)
        }
    }
}