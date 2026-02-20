package io.github.duzhaokun123.yabr

import io.github.duzhaokun123.codegen.ModuleEntries
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.logger.AndroidLogger
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.hookerContext
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.setFieldValue

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

        val hostClassLoader = loaderContext.hostClassloader
        if (hostClassLoader != this.javaClass.classLoader) {
            val selfParentClassLoader = this.javaClass.classLoader?.parent
            val mergedClassLoader = MergedClassLoader(hostClassLoader, selfParentClassLoader)
            this.javaClass.classLoader!!.setFieldValue("parent", mergedClassLoader)
            AndroidLogger.d("merge class loader")
        } else {
            AndroidLogger.d("self classloader same as host classloader, no classloader isolation!")
        }
        allModule = ModuleEntries.entries.toList()
        val target =
            if (":" in loader.processName) loader.processName.drop(loader.processName.indexOf(':') + 1) else ""
        val toLoad =
            allModule
                .map { it to it.metadata }
                .filter { (_, metadata) ->
                    metadata.targets.isEmpty() || target in metadata.targets
                }
        
        val sortedToLoad = topologicalSort(toLoad)
        
        currentProcessModule = sortedToLoad.map { it.first }
        val coreModules =
            sortedToLoad
                .filter { (module, _) -> module is Core }
                .map { it.first }
        val otherModules =
            sortedToLoad
                .map { it.first }
                .minus(coreModules.toSet())

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
        val fullModuleName = "${module.id}(${module::class.java.name})[${module.metadata.priority}]"
        logger.i("Loading module: $fullModuleName")
        if (module.loaded) {
            logger.w("Module $fullModuleName is already loaded.")
            return
        }
        loadedModule += module
        onModuleLoadListeners.forEach { listener ->
            listener(module)
        }
        runCatching {
            module.onLoad()
        }.onFailure {
            logger.e("Failed to load module: $fullModuleName")
            logger.e(it)
        }.onSuccess { r ->
            if (r != true) {
                logger.w("Module $fullModuleName did not load successfully, but no exception was thrown.")
            } else {
                logger.v("Loaded module: $fullModuleName")
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
        val fullModuleName = "${module.id}(${module::class.java.name})[${module.metadata.priority}]"
        logger.i("Unloading module: $fullModuleName")
        if (module.canUnload.not()) {
            logger.w("Module $fullModuleName cannot be unloaded.")
            return
        }
        if (module is Core) {
            logger.w("Core module $fullModuleName unloading")
        }
        loadedModule -= module
        runCatching {
            module.onUnload()
        }.onFailure {
            logger.e("Failed to unload module: $fullModuleName")
            logger.e(it)
        }.onSuccess { r ->
            if (r != true) {
                logger.w("Module $fullModuleName did not unload successfully, but no exception was thrown.")
            } else {
                logger.v("Unloaded module: $fullModuleName")
            }
        }
        module.loaded = false
    }

    private fun topologicalSort(modules: List<Pair<BaseModule, io.github.duzhaokun123.module.base.ModuleEntry>>): List<Pair<BaseModule, io.github.duzhaokun123.module.base.ModuleEntry>> {
        val result = mutableListOf<Pair<BaseModule, io.github.duzhaokun123.module.base.ModuleEntry>>()
        
        val groupedByPriority = modules.groupBy { it.second.priority }.toSortedMap()
        
        for ((priority, groupModules) in groupedByPriority) {
            val visited = mutableSetOf<String>()
            val visiting = mutableSetOf<String>()
            val groupModuleMap = groupModules.associateBy { it.first.id }

            fun visit(moduleId: String) {
                if (moduleId in visited) return
                if (moduleId in visiting) {
                    logger.w("Circular dependency detected involving module: $moduleId in priority group $priority")
                    return
                }
                
                val modulePair = groupModuleMap[moduleId]
                if (modulePair != null) {
                    visiting.add(moduleId)
                    for (dep in modulePair.second.dependencies) {
                        // Only resolve dependencies within the same priority group
                        if (groupModuleMap.containsKey(dep)) {
                            visit(dep)
                        }
                    }
                    result.add(modulePair)
                    visiting.remove(moduleId)
                    visited.add(moduleId)
                }
            }

            for (modulePair in groupModules) {
                visit(modulePair.first.id)
            }
        }

        return result
    }

    private val onModuleLoadListeners = mutableListOf<(BaseModule) -> Unit>()

    fun addOnModuleLoadListener(listener: (BaseModule) -> Unit) {
        onModuleLoadListeners.add(listener)
    }
}

