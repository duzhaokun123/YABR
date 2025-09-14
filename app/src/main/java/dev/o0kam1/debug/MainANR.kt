package dev.o0kam1.debug

import android.content.Context
import android.view.View
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.UIComplex

@ModuleEntry(
    id = "dev.o0kam1.debug.MainANR"
)
object MainANR : BaseModule(), UIComplex {
    override val name = "ANR"
    override val description = "Thread.sleep(Long.MAX_VALUE) in main thread"
    override val category = UICategory.DEBUG

    override fun onLoad(): Boolean {
        return true
    }

    override fun onCreateUI(context: Context): View {
        logger.d("ANR start")
        Thread.sleep(Long.MAX_VALUE)
        return View(context)
    }
}