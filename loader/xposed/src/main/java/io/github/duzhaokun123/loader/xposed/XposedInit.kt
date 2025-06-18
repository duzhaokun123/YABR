package io.github.duzhaokun123.loader.xposed

import android.app.AndroidAppHelper
import android.app.Application
import android.app.Instrumentation
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.duzhaokun123.hooker.xposed.XposedHookerContext
import io.github.duzhaokun123.loader.base.ImplementationInfo
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.Main

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {
    lateinit var startupParam: IXposedHookZygoteInit.StartupParam

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.isFirstApplication.not()) return

        XposedBridge.log("XposedInit: ${lpparam.packageName} (${lpparam.processName})")

        val xposedTag = XposedBridge::class.java.getField("TAG").get(null)

        val loaderContext = object : LoaderContext {
            override val implementationInfo: ImplementationInfo
                get() = ImplementationInfo(
                    name = "XposedLegacy",
                    version = "${XposedBridge.getXposedVersion()}",
                    description = "Xposed Framework, with log TAG $xposedTag"
                )
            override val processName: String
                get() = lpparam.processName
            override val application: Application
                get() = AndroidAppHelper.currentApplication()
            override val hostClassloader: ClassLoader
                get() = lpparam.classLoader
            override val modulePath: String
                get() = startupParam.modulePath
        }
        val hookerContext = XposedHookerContext(lpparam)

        XposedHelpers.findAndHookMethod(
            Instrumentation::class.java, "callApplicationOnCreate",
            Application::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    Main.main(loaderContext, hookerContext)
                }
            })
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        this.startupParam = startupParam
    }
}