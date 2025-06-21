package io.github.duzhaokun123.yabr.utils

import android.os.Handler
import android.os.Looper
import io.github.duzhaokun123.yabr.module.core.BiliToast
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import android.widget.Toast as AndroidToast

object Toast {
    val handler by lazy { Handler(Looper.getMainLooper()) }

    fun show(message: String, duration: Int = AndroidToast.LENGTH_SHORT) {
        handler.post {
            if (BiliToast.showToast(message, duration)) {
                return@post
            } else {
                AndroidToast.makeText(
                    ActivityUtils.topActivity ?: loaderContext.application, message, duration
                ).show()
            }
        }
    }
}