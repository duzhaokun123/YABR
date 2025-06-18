package io.github.duzhaokun123.loader.xposed100

import android.app.Application
import android.app.Instrumentation
import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.hooker.base.Unhooker
import io.github.duzhaokun123.hooker.xposed100.Xposed100HookerContext
import io.github.duzhaokun123.loader.base.ImplementationInfo
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.Main
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.lang.reflect.Member

class ModuleMain(
    val base: XposedInterface,
    val param: XposedModuleInterface.ModuleLoadedParam
) : XposedModule(base, param) {
    companion object {
        @JvmStatic
        lateinit var onApplicationReady: (Application) -> Unit
    }

    lateinit var application: Application

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (param.isFirstPackage.not()) return

        log("ModuleMain: ${param.packageName} ${this.param.processName}")

        val loaderContext = object : LoaderContext {
            override val implementationInfo: ImplementationInfo
                get() = ImplementationInfo(
                    name = "Xposed100",
                    version = "${frameworkVersion}($frameworkVersionCode)",
                    description = "$frameworkName at privilege ${frameworkPrivilege.toPrivilegeType()}"
                )
            override val hostClassloader: ClassLoader
                get() = param.classLoader
            override val processName: String
                get() = this@ModuleMain.param.processName
            override val application: Application
                get() = this@ModuleMain.application
            override val modulePath: String
                get() = applicationInfo.sourceDir
        }

        Xposed100HookerContext.init(base)
        val hookerContext = Xposed100HookerContext

        onApplicationReady = { application ->
            this.application = application
            Main.main(loaderContext, hookerContext)
        }

        Instrumentation::class.java
            .getDeclaredMethod("callApplicationOnCreate", Application::class.java)
            .let {
                hook(it, ApplicationHooker::class.java)
            }
    }

    fun Int.toPrivilegeType(): String {
        return when (this) {
            FRAMEWORK_PRIVILEGE_ROOT -> "FRAMEWORK_PRIVILEGE_ROOT($this)"
            FRAMEWORK_PRIVILEGE_CONTAINER -> "FRAMEWORK_PRIVILEGE_CONTAINER($this)"
            FRAMEWORK_PRIVILEGE_APP -> "FRAMEWORK_PRIVILEGE_APP($this)"
            FRAMEWORK_PRIVILEGE_EMBEDDED -> "FRAMEWORK_PRIVILEGE_EMBEDDED($this)"
            else -> "UNKNOWN($this)"
        }
    }
}
