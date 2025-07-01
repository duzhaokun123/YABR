package io.github.duzhaokun123.yabr.injector.xposed

import android.app.Application
import dalvik.system.DexClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedInit : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            Application::class.java,
            "onCreate",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val application = param.thisObject as Application
                    val pm = application.packageManager
                    val yabrInfo = pm.getApplicationInfo("io.github.duzhaokun123.yabr", 0)
                    val yabrPath = yabrInfo.sourceDir
                    XposedBridge.log("yabr path: ${yabrPath}")
                    val yabrClassLoader = DexClassLoader(
                        yabrPath, application.codeCacheDir.path, null, null
                    )
                    val class_InlineEntry = yabrClassLoader.loadClass("io.github.duzhaokun123.loader.inline.InlineEntry")
                    XposedHelpers.setStaticObjectField(class_InlineEntry, "application", application)
                    XposedHelpers.setStaticObjectField(class_InlineEntry, "previousStageLoader", "xposed")
                    XposedHelpers.callStaticMethod(class_InlineEntry, "entry1", "pine")
                }
            })
    }
}