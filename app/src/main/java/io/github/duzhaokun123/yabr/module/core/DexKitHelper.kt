package io.github.duzhaokun123.yabr.module.core

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.BuildConfig
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.utils.EarlyUtils
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.toClass
import io.github.duzhaokun123.yabr.utils.toConstructor
import io.github.duzhaokun123.yabr.utils.toField
import io.github.duzhaokun123.yabr.utils.toMethod
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.wrap.DexClass
import org.luckypray.dexkit.wrap.DexField
import org.luckypray.dexkit.wrap.DexMethod
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KProperty
import kotlin.text.removePrefix
import kotlin.text.startsWith

@ModuleEntry(
    id = "dexkit_helper",
    priority = 2,
)
object DexKitHelper : BaseModule(), Core, UIComplex {
    override val name = "DexKit 信息"
    override val description = "DexKit 查找结果"
    override val category = UICategory.DEBUG

    var failsafe = false

    override fun onCreateUI(context: Context): View {
        val sv = ScrollView(context)
        val ll = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        sv.addView(ll)
        val tv = TextView(context).apply {
            if (failsafe) {
                val text = SpannableString("failsafe mode").apply {
                    setSpan(ForegroundColorSpan(Color.RED), 0, length, SPAN_INCLUSIVE_EXCLUSIVE)
                    setSpan(AbsoluteSizeSpan(32, true), 0, length, SPAN_INCLUSIVE_EXCLUSIVE)
                }
                append(text)
                append("\n")
            }
            dexFindInfo.forEach { (name, value) ->
                val text = SpannableString("$name:\n\t${value.member}\n\n").apply {
                    if (value.member == null) {
                        setSpan(ForegroundColorSpan(Color.RED), 0, length, SPAN_INCLUSIVE_EXCLUSIVE)
                    }
                }
                append(text)
            }
            setTextIsSelectable(true)
        }
        val btn = Button(context).apply {
            text = "无效化缓存"
            setOnClickListener {
                dexkitCache.putString("version", "cleared")
                Toast.show("下次启动清除缓存")
            }
        }
        ll.addView(tv)
        ll.addView(btn)
        return sv
    }

    var dexKitBridge: DexKitBridge? = null
        private set

    val dexFindInfo = mutableMapOf<String, DexKitMember<*>>()

    val dexkitCache by lazy { ConfigStore.ofModule(this) }

    override fun onLoad(): Boolean {
        val failsafeFile = loaderContext.application.getExternalFilesDir(null)!!
            .resolve("yabr_failsafe")
            .resolve("dexkit")
        if (failsafeFile.exists()) {
            logger.w("dexkit failsafe! no load libdexkit.so")
            failsafe = true
        } else {
            EarlyUtils.loadLibrary("dexkit")
        }
        val pm = loaderContext.application.packageManager
        val packageInfo = pm.getPackageInfo(loaderContext.application.packageName, 0)
        @Suppress("DEPRECATION")
        val newVersion =
            "${packageInfo.packageName}_${packageInfo.versionCode}_${BuildConfig.BUILD_TIME}"
        val oldVersion = dexkitCache.getString("version", null)
        if (oldVersion != newVersion) {
            logger.d("DexKit cache version changed $oldVersion -> $newVersion, clearing cache")
            dexkitCache.clear()
            dexkitCache.putString("version", newVersion)
        }
        Main.addOnModuleLoadListener { module ->
            if (module is DexKitMemberOwner) {
                if (module.needDexKitBridge) {
                    prepareDexKitBridge()
                    runCatching {
                        module.onDexKitReady(dexKitBridge!!)
                    }.onFailure { t ->
                        module.logger.e("Failed to initialize DexKit context: ${module.id}")
                        module.logger.e(t)
                    }
                }
                module.javaClass.declaredFields
                    .filter { it.type == DexKitMember::class.java }
                    .mapNotNull {
                        it.isAccessible = true
                        val dexKitMember =
                            it.get(module) as? DexKitMember<*> ?: return@mapNotNull null
                        if (dexKitMember.cacheable) {
                            if (applyDexKitMemberCache(dexKitMember)) {
                                return@mapNotNull dexKitMember
                            }
                        }
                        runCatching {
                            prepareDexKitBridge()
                            dexKitMember.onBridgeReady(dexKitBridge!!)
                        }.onFailure { t ->
                            module.logger.e("Failed to initialize DexKit member: ${dexKitMember.name}")
                            module.logger.e(t)
                        }
                        if (dexKitMember.cacheable) {
                            saveDexKitMemberCache(dexKitMember)
                        }
                        return@mapNotNull dexKitMember
                    }.forEach {
                        dexFindInfo[it.name] = it
                    }
            }
        }
        return true
    }

    fun applyDexKitMemberCache(dexKitMember: DexKitMember<*>): Boolean {
        val cachedString = dexkitCache.getString(dexKitMember.name)
        if (cachedString != null) {
            runCatching {
                val cacheValue = when {
                    cachedString.startsWith("null") -> null
                    cachedString.startsWith("string:") -> cachedString.removePrefix("string:")
                    cachedString.startsWith("method:") -> DexMethod(cachedString.removePrefix("method:")).toMethod()
                    cachedString.startsWith("constructor:") -> DexMethod(cachedString.removePrefix("constructor:")).toConstructor()
                    cachedString.startsWith("class:") -> DexClass(cachedString.removePrefix("class:")).toClass()
                    cachedString.startsWith("field:") -> DexField(cachedString.removePrefix("field:")).toField()
                    else -> throw RuntimeException("Unknown cache sting: $cachedString")
                }
                dexKitMember.onCacheFound(cacheValue)
                return true
            }.onFailure { t ->
                logger.e("Failed to parse cached value for ${dexKitMember.name}", t)
            }
        }
        return false
    }

    fun saveDexKitMemberCache(dexKitMember: DexKitMember<*>) {
        val name = dexKitMember.name
        val value = dexKitMember.member
        when (value) {
            null -> dexkitCache.putString(name, "null")
            is String -> dexkitCache.putString(name, "string:$value")
            is Method -> dexkitCache.putString(name, "method:${DexMethod(value)}")
            is Constructor<*> -> dexkitCache.putString(name, "constructor:${DexMethod(value)}")
            is Class<*> -> dexkitCache.putString(name, "class:${DexClass(value)}")
            is Field -> dexkitCache.putString(name, "field:${DexField(value)}")
            else -> {
                logger.e("unable to cache ${name} of type ${value.javaClass.name}, no way to serialize it")
            }
        }
    }

    fun prepareDexKitBridge() {
        logger.d("Preparing DexKitBridge")
        if (dexKitBridge == null) {
            logger.d("Creating DexKitBridge")
            dexKitBridge = DexKitBridge.create(loaderContext.application.applicationInfo.sourceDir)
        } else {
            logger.d("DexKitBridge already exists")
        }
        Toast.handler.removeCallbacks(closeCallback)
        Toast.handler.postDelayed(closeCallback, 1000L)
    }

    val closeCallback = Runnable {
        dexKitBridge?.close()
        dexKitBridge = null
        logger.d("DexKitBridge closed")
    }
}

class DexKitMember<T>(
    val name: String,
    val cacheable: Boolean,
    val init: (DexKitBridge) -> T
) {
    var member: T? = null
        private set

    @Suppress("UNCHECKED_CAST")
    fun onCacheFound(member: Any?) {
        this.member = member as T?
    }

    fun onBridgeReady(bridge: DexKitBridge) {
        member = init(bridge)
    }

    operator fun getValue(t: Any?, property: KProperty<*>): T? {
        return member
    }
}
