package dev.o0kam1.tools

import android.os.Handler
import android.os.Looper
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.loaderContext
import io.github.duzhaokun123.yabr.utils.loop
import io.github.duzhaokun123.yabr.utils.reason

@ModuleEntry(
    id = "dev.o0kam1.tools.CrashHandle",
    targets = [ModuleEntryTarget.MAIN]
)
object CrashHandle : BaseModule(), UISwitch, SwitchModule {
    override val canUnload = false
    override val name = "防止 jvm 层崩溃"
    override val description = "Let it Crash 对用户来说并不是什么好主意, 特别是模块把宿主搞炸了\n" +
            "当然不崩溃不代表功能就能正常用了"
    override val category = UICategory.TOOL

    override fun onLoad(): Boolean {
        Handler(Looper.getMainLooper()).post {
            loop {
                runCatching {
                    Looper.loop()
                }.onFailure { throwable ->
                    handleException(Thread.currentThread(), throwable)
                }
            }
        }
        Thread.currentThread().setUncaughtExceptionHandler(::handleException)
        return true
    }

    fun handleException(thread: Thread, throwable: Throwable) {
        logger.e("Uncaught exception in ${loaderContext.processName} in ${thread.name}", throwable)
        Toast.show("未捕获异常在 进程 ${loaderContext.processName} 线程 ${thread.name}\n${throwable.reason}")
    }
}