package io.github.duzhaokun123.yabr.logger

import android.app.Activity

class ActivityLogger(
    val className: String
): Logger {
    override fun writeText(
        level: Logger.Level, text: String
    ) {
        AndroidLogger.writeText(level, "[$className] $text")
    }
}

val Activity.activityLogger
    get() = ActivityLogger(this::class.java.name)
