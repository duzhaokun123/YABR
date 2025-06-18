package io.github.duzhaokun123.yabr.module.base

import android.content.Context
import android.view.View

interface UIComplex : UIEntry {
    fun onCreateUI(context: Context): View
}