package io.github.duzhaokun123.yabr

import io.github.duzhaokun123.codegen.ModuleEntries
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.logger.AndroidLogger
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.hookerContext
import io.github.duzhaokun123.yabr.utils.loaderContext

object Main {
    const val packageName = BuildConfig.APPLICATION_ID
    var allModule: List<BaseModule> = emptyList()
        private set
    var currentProcessModule: List<BaseModule> = emptyList()
        private set
    var loadedModule: List<BaseModule> = listOf()
        private set

    val logger = AndroidLogger

    fun main(loader: LoaderContext, hooker: HookerContext) {
        logger.i("Main started on ${loader.application.packageName} ${loader.processName}")
        logger.i(loader.implementationInfo)
        logger.i(hooker.implementationInfo)
        logger.i("module path: ${loader.modulePath}")

        loaderContext = loader
        hookerContext = hooker

        allModule = ModuleEntries.entries
        val target =
            if (":" in loader.processName) loader.processName.drop(loader.processName.indexOf(':') + 1) else ""
        val toLoad =
            ModuleEntries.entries
                .map { it to it.metadata }
                .filter { (_, metadata) ->
                    metadata.targets.isEmpty() || target in metadata.targets
                }.sortedBy { (_, metadata) ->
                    metadata.priority
                }
        currentProcessModule = toLoad.map { it.first }
        val coreModules =
            toLoad
                .filter { (module, _) -> module is Core }
                .map { it.first }
        val otherModules =
            toLoad
                .map { it.first }
                .minus(coreModules)

        logger.i("loading core modules...")
        coreModules.forEach {
            loadModule(it)
        }
        logger.i("core modules loaded.")
        logger.i("loading other modules...")
        otherModules.forEach {
            if (it is SwitchModule && it.isEnabled.not()) {
                return@forEach
            }
            loadModule(it)
        }
        logger.i("other modules loaded.")
    }

    fun loadModule(module: BaseModule) {
        logger.i("Loading module: ${module.id}(${module::class.java.name})[${module.metadata.priority}]")
        if (module.loaded) {
            logger.w("Module ${module.id} is already loaded.")
            return
        }
        loadedModule += module
        onModuleLoadListeners.forEach { listener ->
            listener(module)
        }
        runCatching {
            module.onLoad()
        }.onFailure {
            logger.e("Failed to load module: ${module::class.java.name}")
            logger.e(it)
        }.onSuccess { r ->
            if (r != true) {
                logger.w("Module ${module::class.java.name} did not load successfully, but no exception was thrown.")
            }
        }
        module.loaded = true
        if (module is Compatible) {
            val uncompatibleReason = runCatching {
                module.checkCompatibility()
            }.getOrElse { t ->
                t.localizedMessage ?: t.message ?: "Unknown error"
            }
            if (uncompatibleReason != null) {
                logger.w("Module ${module.id} not compatible: $uncompatibleReason")
                Toast.show("模块 ${module.id} 不兼容: $uncompatibleReason")
            }
        }
    }

    fun unloadModule(module: BaseModule) {
        logger.i("Unloading module: ${module.id}(${module::class.java.name})")
        if (module.canUnload.not()) {
            logger.w("Module ${module.id} cannot be unloaded.")
            return
        }
        if (module is Core) {
            logger.w("Core module ${module.id} unloading")
        }
        loadedModule -= module
        runCatching {
            module.onUnload()
        }.onFailure {
            logger.e("Failed to unload module: ${module::class.java.name}")
            logger.e(it)
        }.onSuccess { r ->
            if (r != true) {
                logger.w("Module ${module::class.java.name} did not unload successfully, but no exception was thrown.")
            }
        }
        module.loaded = false
    }

    private val onModuleLoadListeners = mutableListOf<(BaseModule) -> Unit>()

    fun addOnModuleLoadListener(listener: (BaseModule) -> Unit) {
        onModuleLoadListeners.add(listener)
    }
}