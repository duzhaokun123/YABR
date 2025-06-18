package io.github.duzhaokun123.hooker.xposed100;

import io.github.libxposed.api.XposedInterface;

public class Hooker implements XposedInterface.Hooker {
    public static void before(XposedInterface.BeforeHookCallback callback) {
        HookerKt.before(callback);
    }

    public static void after(XposedInterface.AfterHookCallback callback) {
        if (callback.isSkipped()) return;
        HookerKt.after(callback);
    }
}
