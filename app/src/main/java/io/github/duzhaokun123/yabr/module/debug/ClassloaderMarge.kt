package io.github.duzhaokun123.yabr.module.debug

import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.logger.AndroidLogger
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIEntry
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.setFieldValue

@ModuleEntry(
    id = "classloader_merge",
    priority = 1
)
object ClassloaderMerge: BaseModule(), Core, UIEntry {
    override val name = "classloader 融合"
    override val description = "使模块可直接使用宿主类"
    override val category = UICategory.DEBUG

    override fun onLoad(): Boolean {
        val hostClassLoader = loaderContext.hostClassloader
        val selfParentClassLoader = this.javaClass.classLoader!!.parent
        val mergedClassLoader = MergedClassLoader(hostClassLoader, selfParentClassLoader)
        this.javaClass.classLoader!!.setFieldValue("parent", mergedClassLoader)
        return true
    }
}

class MergedClassLoader(
    val hostClassLoader: ClassLoader,
    val selfParentClassLoader: ClassLoader
): ClassLoader() {
    override fun loadClass(name: String): Class<*>? {
        if (name.startsWith("androidx.")) {
//            AndroidLogger.d(name)
            return hostClassLoader.loadClass(name)
        }
        runCatching {
            return selfParentClassLoader.loadClass(name)
        }
        runCatching {
            return hostClassLoader.loadClass(name)
        }
        throw ClassNotFoundException("$name not found in $hostClassLoader $selfParentClassLoader")
    }
}