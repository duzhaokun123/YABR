package io.github.duzhaokun123.loader.libxposed

import android.app.Application
import android.app.Instrumentation
import android.util.Log
import io.github.duzhaokun123.hooker.libxposed.LibXposedHookerContext
import io.github.duzhaokun123.loader.base.ImplementationInfo
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.Main
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class LibXposedInit : XposedModule() {
    lateinit var processName: String
    lateinit var application: Application
    val frameworkPropertiesTypes = mutableListOf<String>()

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        processName = param.processName
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.isFirstPackage.not()) return

        log(Log.INFO, "LibXposedInit", "${param.packageName} ($processName)")

        if ((frameworkProperties and PROP_CAP_SYSTEM) != 0L) {
            frameworkPropertiesTypes.add("PROP_CAP_SYSTEM")
        }
        if ((frameworkProperties and PROP_CAP_REMOTE) != 0L) {
            frameworkPropertiesTypes.add("PROP_CAP_REMOTE")
        }
        if ((frameworkProperties and PROP_RT_API_PROTECTION) != 0L) {
            frameworkPropertiesTypes.add("PROP_RT_API_PROTECTION")
        }
        val loaderContext = object : LoaderContext {
            override val implementationInfo: ImplementationInfo
                get() = ImplementationInfo(
                    name = "LibXposed",
                    version = "$apiVersion",
                    description = "$frameworkName $frameworkVersion ($frameworkVersionCode)\n" +
                            "properties: [${frameworkPropertiesTypes.joinToString(", ")}]"
                )
            override val hostClassloader: ClassLoader
                get() = param.classLoader
            override val processName: String
                get() = this@LibXposedInit.processName
            override val application: Application
                get() = this@LibXposedInit.application
            override val modulePath: String
                get() = moduleApplicationInfo.sourceDir
        }
        val hookerContext = LibXposedHookerContext(this)

        hook(Instrumentation::class.java.getDeclaredMethod("callApplicationOnCreate", Application::class.java))
            .intercept { chain ->
                application = chain.args[0] as Application
                Main.main(loaderContext, hookerContext)
                return@intercept chain.proceed()
            }
    }

    override fun onHotReloading(param: XposedModuleInterface.HotReloadingParam): Boolean {
        log(Log.INFO, "LibXposedInit", "onHotReloading")
        return false
    }
}
