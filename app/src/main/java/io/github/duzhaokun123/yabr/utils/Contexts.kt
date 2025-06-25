package io.github.duzhaokun123.yabr.utils

import android.content.Context
import io.github.duzhaokun123.yabr.R

object Contexts {
    fun Context.createAppThemeWrapper(): Context {
        return android.view.ContextThemeWrapper(this, R.style.AppTheme)
    }

    fun Context.createAppCompatThemeWrapper(): Context {
        return androidx.appcompat.view.ContextThemeWrapper(
            this, androidx.appcompat.R.style.Theme_AppCompat
        )
    }
}