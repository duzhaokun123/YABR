package io.github.duzhaokun123.yabr.compat.biliroaming

import android.widget.Toast as AndroidToast
import io.github.duzhaokun123.yabr.logger.AndroidLogger
import io.github.duzhaokun123.yabr.utils.Toast

object Log {
    @JvmStatic
    fun d(obj: Any?) {
        AndroidLogger.d(obj)
    }

    @JvmStatic
    fun i(obj: Any?) {
        AndroidLogger.i(obj)
    }

    @JvmStatic
    fun e(obj: Any?) {
        AndroidLogger.e(obj)
    }

    @JvmStatic
    fun v(obj: Any?) {
        AndroidLogger.v(obj)
    }

    @JvmStatic
    fun w(obj: Any?) {
        AndroidLogger.w(obj)
    }

    /**
     * @param force 被忽略
     */
    fun toast(msg: String, force: Boolean = false, duration: Int = AndroidToast.LENGTH_LONG, alsoLog: Boolean = true) {
        Toast.show(msg, duration)
        if (alsoLog) {
            w(msg)
        }
    }
}