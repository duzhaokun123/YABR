package dev.o0kam1

import android.widget.Toast
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.utils.findMethod

@ModuleEntry(
    id = "dev.o0kam1.Test",
)
object Test : BaseModule() {
    override fun onLoad(): Boolean {
//        Toast::class.java
//            .findMethod { it.name == "show" }
//            .hookBefore {
//                logger.d(Thread.currentThread().stackTrace.joinToString("\n"))
//            }
        return true
    }
}