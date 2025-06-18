package io.github.duzhaokun123.yabr.module.test

import android.content.Context
import android.graphics.Color
import android.view.View
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.base.UISwitch

@ModuleEntry(
    id = "c"
)
object C : BaseModule(), UISwitch, UIComplex, SwitchModule {
    override fun onLoad(): Boolean {
        return true
    }

    override val name: CharSequence
        get() = "C Module"
    override val description: CharSequence
        get() = "This is a test module C"
    override val category: String
        get() = "test"

    override fun onCreateUI(context: Context): View {
        return View(context).apply {
            setBackgroundColor(Color.RED)
        }
    }
}