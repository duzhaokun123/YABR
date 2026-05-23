package io.github.duzhaokun123.hooker.libxposed

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.github.duzhaokun123.hooker.base.HookCallback
import io.github.duzhaokun123.hooker.base.HookerContext
import io.github.duzhaokun123.hooker.base.ImplementationInfo
import io.github.duzhaokun123.hooker.base.Unhooker
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Member
import java.lang.reflect.Method

@RequiresApi(Build.VERSION_CODES.O)
class LibXposedHookerContext(
    private val xposed: XposedInterface
) : HookerContext {
    val frameworkPropertiesTypes = mutableListOf<String>()

    init {
        if ((xposed.frameworkProperties and XposedInterface.PROP_CAP_SYSTEM) != 0L) {
            frameworkPropertiesTypes.add("PROP_CAP_SYSTEM")
        }
        if ((xposed.frameworkProperties and XposedInterface.PROP_CAP_REMOTE) != 0L) {
            frameworkPropertiesTypes.add("PROP_CAP_REMOTE")
        }
        if ((xposed.frameworkProperties and XposedInterface.PROP_RT_API_PROTECTION) != 0L) {
            frameworkPropertiesTypes.add("PROP_RT_API_PROTECTION")
        }
    }

    override val implementationInfo: ImplementationInfo
        get() = ImplementationInfo(
            name = "LibXposed",
            version = "${xposed.apiVersion}",
            description = "${xposed.frameworkName} ${xposed.frameworkVersion} (${xposed.frameworkVersionCode})\n" +
                    "properties: [${frameworkPropertiesTypes.joinToString(", ")}]"
        )

    override fun hookMethod(
        method: Member,
        callback: HookCallback
    ): Unhooker {
        val handle = xposed.hook(method as Executable)
            .setExceptionMode(XposedInterface.ExceptionMode.PASSTHROUGH)
            .intercept { chain ->
                val invoker = when (val executable = chain.executable) {
                    is Method -> xposed
                        .getInvoker(executable)
                        .setType(XposedInterface.Invoker.Type.ORIGIN)

                    is Constructor<*> -> xposed
                        .getInvoker(executable)
                        .setType(XposedInterface.Invoker.Type.ORIGIN)

                    else -> throw IllegalArgumentException("Unsupported executable: ${executable.javaClass.name}")
                }

                val context = LibXposedHookCallbackContext(chain, invoker)
                runCatching {
                    callback.before(context)
                }.onFailure { e ->
                    xposed.log(
                        Log.WARN, "LibXposedHookerContext", "Failed to execute before callback", e
                    )
                }

                if (context.shouldSkipOriginal()) {
                    context.throwIfNeeded()
                    return@intercept context.result
                }

                try {
                    val result = chain.proceed(context.args)
                    context.result = result
                } catch (t: Throwable) {
                    context.throwable = t
                }

                runCatching {
                    callback.after(context)
                }.onFailure { e ->
                    xposed.log(
                        Log.WARN, "LibXposedHookerContext", "Failed to execute after callback", e
                    )
                }

                context.throwIfNeeded()
                return@intercept context.result
            }
        return {
            handle.unhook()
        }
    }

    override fun invokeOriginal(
        method: Member,
        thiz: Any?,
        vararg args: Any?
    ): Any? {
        return when (method) {
            is Method -> xposed
                .getInvoker(method)
                .setType(XposedInterface.Invoker.Type.ORIGIN)
                .invoke(thiz, *args)

            is Constructor<*> -> xposed
                .getInvoker(method)
                .setType(XposedInterface.Invoker.Type.ORIGIN)
                .newInstance(*args)

            else -> throw IllegalArgumentException("Unsupported member type: ${method.javaClass.name}")
        }
    }

    override fun <T> newInstanceOriginal(
        constructor: Constructor<T>,
        vararg args: Any?
    ): T {
        return xposed
            .getInvoker(constructor)
            .setType(XposedInterface.Invoker.Type.ORIGIN)
            .newInstance(*args)
    }
}
