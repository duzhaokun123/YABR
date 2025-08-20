package io.github.qauxv.chainloader.api;

import android.app.Application;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import io.github.qauxv.loader.hookapi.IHookBridge;

@Keep
public class ChainLoaderAgent {

    private ChainLoaderAgent() {
        throw new AssertionError("no instance");
    }

    @NonNull
    public static ClassLoader getModuleClassLoader() {
        throw new AssertionError("Stub!");
    }

    @NonNull
    public static ClassLoader getHostClassLoader() {
        throw new AssertionError("Stub!");
    }

    @NonNull
    public static Application getHostApplication() {
        throw new AssertionError("Stub!");
    }

    @NonNull
    public static IHookBridge getHookBridge() {
        throw new AssertionError("Stub!");
    }

    @NonNull
    public static String getProcessName() {
        throw new AssertionError("Stub!");
    }

}
