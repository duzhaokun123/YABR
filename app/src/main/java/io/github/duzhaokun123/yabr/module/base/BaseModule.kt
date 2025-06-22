package io.github.duzhaokun123.yabr.module.base

import androidx.annotation.CallSuper
import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.duzhaokun123.hooker.base.Unhooker
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.logger.AndroidLogger
import io.github.duzhaokun123.yabr.logger.Logger
import io.github.duzhaokun123.yabr.utils.hookerContext
import io.github.duzhaokun123.yabr.utils.loadClass
import java.lang.reflect.Member

abstract class BaseModule {
    val logger = object : Logger {
        override fun writeText(
            level: Logger.Level, text: String
        ) {
            AndroidLogger.writeText(level, "[$id] $text")
        }
    }
    val id: String
        get() = metadata.id
    val metadata by lazy { this::class.java.getAnnotation(ModuleEntry::class.java)!! }
    open val canUnload: Boolean = this !is Core
    val unhookers = mutableListOf<Unhooker>()
    var loaded = false

    abstract fun onLoad(): Boolean

    @CallSuper
    open fun onUnload(): Boolean {
        if (canUnload) {
            unhookers.forEach { it.invoke() }
            unhookers.clear()
            return true
        }
        logger.w("Module $id cannot be unloaded")
        return false
    }

    fun Member.hook(callback: HookCallback): Unhooker {
        return hookerContext.hookMethod(this, object : HookCallback {
            override fun before(callbackContext: HookCallbackContext) {
                runCatching {
                    callback.before(callbackContext)
                }.onFailure {
                    logger.e(it)
                }
            }

            override fun after(callbackContext: HookCallbackContext) {
                runCatching {
                    callback.after(callbackContext)
                }.onFailure {
                    logger.e(it)
                }
            }
        }).also { unhookers.add(it) }
    }

    fun Member.hookBefore(callback: (HookCallbackContext) -> Unit): Unhooker {
        return this.hook(object : HookCallback {
            override fun before(callbackContext: HookCallbackContext) {
                callback(callbackContext)
            }
        })
    }

    fun Member.hookAfter(callback: (HookCallbackContext) -> Unit): Unhooker {
        return this.hook(object : HookCallback {
            override fun after(callbackContext: HookCallbackContext) {
                callback(callbackContext)
            }
        })
    }

    fun Member.hookReplace(callback: (HookCallbackContext) -> Any?): Unhooker {
        return this.hook(object : HookCallback {
            override fun before(callbackContext: HookCallbackContext) {
                callbackContext.result = callback(callbackContext)
            }
        })
    }

    fun Iterable<Member>.hook(callback: HookCallback): List<Unhooker> {
        return this.map { it.hook(callback) }
    }

    fun Iterable<Member>.hookBefore(callback: (HookCallbackContext) -> Unit): List<Unhooker> {
        return this.map { it.hookBefore(callback) }
    }

    fun Iterable<Member>.hookAfter(callback: (HookCallbackContext) -> Unit): List<Unhooker> {
        return this.map { it.hookAfter(callback) }
    }

    fun Iterable<Member>.hookReplace(callback: (HookCallbackContext) -> Any?): List<Unhooker> {
        return this.map { it.hookReplace(callback) }
    }

    fun Array<Member>.hook(callback: HookCallback): List<Unhooker> {
        return this.map { it.hook(callback) }
    }

    fun Array<Member>.hookBefore(callback: (HookCallbackContext) -> Unit): List<Unhooker> {
        return this.map { it.hookBefore(callback) }
    }

    fun Array<Member>.hookAfter(callback: (HookCallbackContext) -> Unit): List<Unhooker> {
        return this.map { it.hookAfter(callback) }
    }

    fun Array<Member>.hookReplace(callback: (HookCallbackContext) -> Any?): List<Unhooker> {
        return this.map { it.hookReplace(callback) }
    }

    fun Class<*>.hookAllConstructors(callback: HookCallback): List<Unhooker> {
        return this.constructors.map { it.hook(callback) }
    }

    fun Class<*>.hookAllConstructorsBefore(callback: (HookCallbackContext) -> Unit): List<Unhooker> {
        return this.constructors.map { it.hookBefore(callback) }
    }

    fun Class<*>.hookAllConstructorsAfter(callback: (HookCallbackContext) -> Unit): List<Unhooker> {
        return this.constructors.map { it.hookAfter(callback) }
    }

    fun Result<*>.logError(message: String? = null) {
        onFailure { t ->
            if (message != null) {
                logger.e(message)
            }
            logger.e(t)
        }
    }
}

/**
 * 非常懒 [BaseModule.onLoad] 后也可能不加载 可能导致问题在 [BaseModule.onLoad] 里没有暴露 也可能导致没有问题
 */
fun BaseModule.lazyLoadClass(name: String) = lazy { loadClass(name) }

/**
 * 多个加载 返回 true 全部加载成功 否则 false
 *
 * 顺序执行 不会短路
 */
fun BaseModule.multiLoadAllSuccess(
    vararg loadBlocks: () -> Boolean
): Boolean {
    var success = true
    loadBlocks.forEach { loadBlock ->
        val blockSuccess =
            runCatching {
                loadBlock()
            }.onFailure { t ->
                logger.w(t)
            }.getOrDefault(false)
        if (blockSuccess.not()) {
            logger.w("Load block failed: $loadBlock")
            success = false
        }
    }
    return success
}

/**
 * 多个加载 返回 true 任何一个加载成功 否则 false
 *
 * 顺序执行 不会短路
 */
fun BaseModule.multiLoadAnySuccess(
    vararg loadBlocks: () -> Boolean
): Boolean {
    var success = false
    loadBlocks.forEach { loadBlocks ->
        val blockSuccess =
            runCatching {
                loadBlocks()
            }.onFailure { t ->
                logger.w(t)
            }.getOrDefault(false)
        if (blockSuccess) {
            success = true
        } else {
            logger.w("Load block failed: $loadBlocks")
        }
    }
    return success
}
