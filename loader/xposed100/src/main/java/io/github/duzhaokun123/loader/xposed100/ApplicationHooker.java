package io.github.duzhaokun123.loader.xposed100;

import android.app.Application;

import io.github.libxposed.api.XposedInterface;

public class ApplicationHooker implements XposedInterface.Hooker {
    public static void before(XposedInterface.BeforeHookCallback callback) {
        ModuleMain.onApplicationReady.invoke((Application) callback.getArgs()[0]);
    }

    public static void after(XposedInterface.AfterHookCallback callback) {

    }
}
