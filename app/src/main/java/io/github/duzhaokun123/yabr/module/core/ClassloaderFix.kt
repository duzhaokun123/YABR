package io.github.duzhaokun123.yabr.module.core

import android.content.ContextWrapper
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.Main
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Core
import io.github.duzhaokun123.yabr.utils.findMethod

@ModuleEntry(
    id = "classloader_fix",
)
object ClassloaderFix : BaseModule(), Core {

    override fun onLoad(): Boolean {
        ContextWrapper::class.java
            .findMethod { it.name == "getClassLoader" }
            .hookAfter {
                if (it.result is ListClassLoader) {
                    return@hookAfter
                }
                it.result = ListClassLoader(
                    it.result as ClassLoader,
                    Main::class.java.classLoader
                )
            }
        return true
    }
}

class ListClassLoader(
    vararg val classLoaders: ClassLoader
) : ClassLoader() {
    override fun loadClass(name: String?): Class<*>? {
        classLoaders.forEach {
            runCatching {
                return it.loadClass(name)
            }
        }
        throw ClassNotFoundException("$name not found in ${classLoaders.joinToString(", ")}")
    }

    override fun toString(): String {
        return "ListClassLoader[${classLoaders.joinToString(", ")}]"
    }
}
