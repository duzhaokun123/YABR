package io.github.duzhaokun123.yabr.compat.biliroaming

import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.utils.loaderContext

val BaseModule.mClassLoader: ClassLoader
    get() = loaderContext.hostClassloader