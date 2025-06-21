package io.github.duzhaokun123.yabr.logger

import android.util.Log

object AndroidLogger : Logger {
    const val TAG = "YABR"

    override fun writeText(level: Logger.Level, text: String) {
        when (level) {
            Logger.Level.VERBOSE -> Log.v(TAG, text)
            Logger.Level.DEBUG -> Log.d(TAG, text)
            Logger.Level.INFO -> Log.i(TAG, text)
            Logger.Level.WARN -> Log.w(TAG, text)
            Logger.Level.ERROR -> Log.e(TAG, text)
        }
    }
}