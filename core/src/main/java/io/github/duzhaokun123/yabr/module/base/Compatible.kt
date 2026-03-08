package io.github.duzhaokun123.yabr.module.base

interface Compatible {
    /**
     * 检查当前模块是否兼容
     *
     * @return 如果不兼容，返回一个描述不兼容的字符串；如果兼容，返回 null
     */
    fun checkCompatibility(): String?
}

fun Compatible.requireMinSystem(minVersion: Int): String? {
    val currentVersion = android.os.Build.VERSION.SDK_INT
    return if (currentVersion < minVersion) {
        "requires Android API level $minVersion or higher"
    } else {
        null
    }
}
