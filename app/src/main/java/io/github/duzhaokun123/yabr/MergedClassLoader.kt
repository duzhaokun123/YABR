package io.github.duzhaokun123.yabr

import io.github.duzhaokun123.yabr.logger.AndroidLogger

class MergedClassLoader(
    val hostClassLoader: ClassLoader,
    val selfParentClassLoader: ClassLoader
): ClassLoader() {
    override fun loadClass(name: String): Class<*>? {
        if (name.startsWith("androidx")) {
//            AndroidLogger.d("$name")
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