package io.github.duzhaokun123.yabr.module.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.core.R
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.UIComplex
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
import io.github.duzhaokun123.yabr.utils.allocateInstance
import io.github.duzhaokun123.yabr.utils.findField
import java.lang.reflect.Proxy

data class ThreePointItemItemData(
    val name: String,
    val data: String?
)

interface ThreePointCallback {
    fun parseData(data: Any): ThreePointItemItemData?
    fun onClick(data: ThreePointItemItemData)
}

/**
 * TODO: 使用 [PegasusHook] 进行主页修改
 */
@ModuleEntry(
    id = "three_point_hook",
    targets = [ModuleEntryTarget.MAIN]
)
object ThreePointHook : BaseModule(), Core, DexKitMemberOwner, UIComplex {
    val threePointCallbackMap =
        mutableMapOf<Long, ThreePointCallback>()
    val config by lazy { ConfigStore.ofModule(this) }

    val class_PegasusParser by dexKitMember(
        "com.bilibili.pegasus.request.PegasusParser",
    ) { bridge ->
        bridge.findClass {
            matcher {
                usingStrings("[Pegasus]PegasusParser")
            }
        }.single().toClass()
    }
    val class_ThreePointItem by lazyLoadClass("com.bilibili.app.comm.list.common.data.ThreePointItem")
    val class_DislikeReason by lazyLoadClass("com.bilibili.app.comm.list.common.data.DislikeReason")

    override fun onLoad() =
        multiLoadAnySuccess(::hookPegasus1, ::hookTheseus)

    fun hookPegasus1(): Boolean {
        class_PegasusParser!!
            .findMethod { it.name == "convert" && it.paramCount == 1 && it.parameterTypes[0] == Object::class.java }
            .hookAfter {
                val data = it.result?.getFieldValue("data") ?: return@hookAfter
                hookPegasusFeedConvert(data)
            }
        val class_DislikeRequestRecord = loadClass("Lcom/bilibili/pegasus/data/card/DislikeRequestRecord;")
        val class_DislikeRequestRecord_Dislike = loadClass($$"Lcom/bilibili/pegasus/data/card/DislikeRequestRecord$Dislike;")
        loadClass("com.bilibili.pegasus.ext.threepoint.ThreePointKt")
            .findMethod(findSuper = false) { it.paramCount >= 4 && it.parameterTypes[3] == class_DislikeRequestRecord }
            .hookBefore {
                val dislikeRequestRecord = it.args[3]
                if (class_DislikeRequestRecord_Dislike.isInstance(dislikeRequestRecord) && hookPegasusDislikeReason(dislikeRequestRecord!!.getFieldValue("a"))) {
                    it.result = null
                }
            }
        return true
    }

    val class_DetailRelateService by lazyLoadClass("com.bilibili.ship.theseus.united.page.intro.module.relate.DetailRelateService")
    val class_RelateCard by lazyLoadClass("com.bilibili.ship.theseus.united.page.intro.module.relate.RelateCard")
    val class_RelateCardMoreMenuHelperKt by lazyLoadClass("com.bilibili.ship.theseus.united.page.intro.module.relate.RelateCardMoreMenuHelperKt")
    val class_TheseusDetailRelateMenuService by lazyLoadClass("com.bilibili.ship.theseus.united.page.intro.module.relate.TheseusDetailRelateMenuService")
    val method_DetailRelateService_showRelateCardMoreMenu by dexKitMember(
        "com.bilibili.ship.theseus.united.page.intro.module.relate.DetailRelateService.showRelateCardMoreMenu"
    ) { bridge ->
        bridge.findMethod {
            matcher {
                declaredClass(class_DetailRelateService)
                paramTypes(
                    Rect::class.java,
                    class_RelateCard,
                    Boolean::class.javaPrimitiveType,
                    loadClass("kotlin.jvm.functions.Function1"),
                    loadClass("kotlin.jvm.functions.Function1"),
                    loadClass("kotlin.jvm.functions.Function2")
                )
            }
        }.single().toMethod()
    }
    val method_RelateCardMoreMenuHelper_addMenuItem by dexKitMember(
        "com.bilibili.ship.theseus.united.page.intro.module.relate.RelateCardMoreMenuHelperKt.addMenuItem"
    ) { bridge ->
        bridge.findMethod {
            matcher {
                declaredClass(class_RelateCardMoreMenuHelperKt)
                paramTypes(
                    class_RelateCard,
                    Boolean::class.javaPrimitiveType,
                    class_RelateDislike,
                    Boolean::class.javaPrimitiveType,
                    class_TheseusDetailRelateMenuService,
                    loadClass("kotlin.jvm.functions.Function1"),
                    loadClass("kotlin.jvm.functions.Function1")
                )
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
        val fieldDetailRelateMenuService = class_DetailRelateService.findField {
            it.type == class_TheseusDetailRelateMenuService
        }
        val classFunction1 = loadClass("kotlin.jvm.functions.Function1")
        method_DetailRelateService_showRelateCardMoreMenu!!
            .hookBefore {
                val relateCard = it.args[1] as Any
                val isLongClick = it.args[2] as Boolean
                val relateDislike = class_RelateDislike!!.allocateInstance()
                relateDislike.setFieldValue("a", "YABR") // title
                relateDislike.setFieldValue("b", " menu") // subtitle
                relateDislike.setFieldValue("c", " menu") // closedSubtitle
                relateDislike.setFieldValue("d", "") // pasteText
                relateDislike.setFieldValue("e", "") // closedPasteText
                val dislikeReasons = mutableListOf<Any>()
                val datas = parseData(relateCard)
                datas.forEach { (id, data) ->
                    val relateReason = class_RelateReasons!!.allocateInstance()
                    relateReason.setFieldValue("a", id) // id
                    relateReason.setFieldValue("e", data.name) // name
                    dislikeReasons.add(relateReason)
                }
                if (dislikeReasons.isEmpty()) return@hookBefore
                if (dislikeReasons.size == 1) {
                    val relateReason = class_RelateReasons!!.allocateInstance()
                    relateReason.setFieldValue("e", "placeholder") // name
                    dislikeReasons.add(relateReason)
                }
                relateDislike.setFieldValue("f", dislikeReasons) // dislikeReason
                relateDislike.setFieldValue("g", "") // toast
                relateDislike.setFieldValue("h", "") // closedToast
                val noReportCallback = Proxy.newProxyInstance(
                    loaderContext.hostClassloader,
                    arrayOf(classFunction1)
                ) { _, _, _ -> Unit }
                val callback = Proxy.newProxyInstance(
                    loaderContext.hostClassloader,
                    arrayOf(classFunction1)
                ) { _, _, args ->
                    val cancelDislikeData = args[0]
                    val feedbackId = cancelDislikeData.getFieldValueAs<String?>("d")?.toLongOrNull() ?: return@newProxyInstance Unit
                    val data = datas.find { it.first == feedbackId }?.second ?: return@newProxyInstance Unit
                    callCallback(feedbackId, data)
                }
                method_RelateCardMoreMenuHelper_addMenuItem!!.invoke(
                    null,
                    relateCard,
                    isLongClick,
                    relateDislike,
                    true,
                    it.thiz!!.getFieldValue(fieldDetailRelateMenuService),
                    noReportCallback,
                    callback
                )
            }
        return true
    }

    /**
     * @param id 唯一ID 不要和官方重复 也不要和其他模块重复
     */
    fun addThreePointCallback(
        id: Long, callback: ThreePointCallback
    ) {
        threePointCallbackMap[id] = callback
    }

    fun removeThreePointCallback(id: Long) {
        threePointCallbackMap.remove(id)
    }

    private fun hookPegasusFeedConvert(data: Any) {
        data.getJsonFieldValueAs<ArrayList<Any>>("items").forEach { item ->
            config.getString("pegasus_three_point_version").takeUnless { it.isNullOrEmpty() }?.let {
                item.setJsonFieldValue("three_point_v", it)
            }
            val threePoint = item.getJsonFieldValueAs<MutableList<Any>?>("three_point_v2")
            val reasons = mutableListOf<Any>()
            val threePointItem = class_ThreePointItem.new()
            threePointItem.setJsonFieldValue("title", "YABR")
            threePointItem.setJsonFieldValue("subtitle", "  menu")
            threePointItem.setJsonFieldValue("type", "dislike")
            threePointItem.setJsonFieldValue("reasons", reasons)
            parseData(item).forEach { (id, data) ->
                val dislikeReason = class_DislikeReason.new()
                dislikeReason.setJsonFieldValue("id", id)
                dislikeReason.setJsonFieldValue("name", data.name)
                dislikeReason.setJsonFieldValue("extra", data.data)
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
        return callCallback(id, threePointItemItemData)
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

    private fun callCallback(id: Long, data: ThreePointItemItemData): Boolean {
        runCatching {
            threePointCallbackMap[id]!!.onClick(data)
            return true
        }.onFailure { t ->
            logger.w("ThreePointCallback $id onClick failed")
            logger.w(t)
        }
        return false
    }

    override val name = "三点菜单注入配置"
    override val description = "配置三点菜单注入"
    override val category = UICategory.DEBUG

    @SuppressLint("InflateParams")
    override fun onCreateUI(context: Context): View {
        val view = LayoutInflater.from(context).inflate(R.layout.module_threepointhook, null)
        val etPegasusThreePontVersion = view.findViewById<EditText>(R.id.et_pegasus_three_point_version)
        etPegasusThreePontVersion.setText(config.getString("pegasus_three_point_version"))
        etPegasusThreePontVersion.addTextChangedListener(
            afterTextChanged = { editable ->
                config.putString("pegasus_three_point_version", editable.toString())
            }
        )
        return view
    }
}
