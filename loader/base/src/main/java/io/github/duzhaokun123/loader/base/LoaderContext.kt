package io.github.duzhaokun123.loader.base

import android.app.Application

interface LoaderContext {
    val implementationInfo: ImplementationInfo
    val hostClassloader: ClassLoader
    val processName: String
    val application: Application
    val modulePath: String
}