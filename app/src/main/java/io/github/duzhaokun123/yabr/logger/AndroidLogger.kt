package io.github.duzhaokun123.yabr.logger

import android.util.Log

object AndroidLogger : Logger {
    const val TAG = "YABR"
    override fun log(level: Logger.Level, message: Any?) {
        if (message is Throwable) {
            log(level, message.stackTraceToString())
        } else {
            when (level) {
                Logger.Level.VERBOSE -> Log.v(TAG, message.toString())
                Logger.Level.DEBUG -> Log.d(TAG, message.toString())
                Logger.Level.INFO -> Log.i(TAG, message.toString())
                Logger.Level.WARN -> Log.w(TAG, message.toString())
                Logger.Level.ERROR -> Log.e(TAG, message.toString())
            }
        }
    }
}