package io.github.duzhaokun123.yabr.logger

interface Logger {
    enum class Level {
        DEBUG, INFO, WARN, ERROR, VERBOSE
    }

    fun d(message: Any?) {
        log(Level.DEBUG, message)
    }

    fun i(message: Any?) {
        log(Level.INFO, message)
    }

    fun w(message: Any?) {
        log(Level.WARN, message)
    }

    fun e(message: Any?) {
        log(Level.ERROR, message)
    }

    fun v(message: Any?) {
        log(Level.VERBOSE, message)
    }

    fun log(level: Level, message: Any?) {
        writeText(
            level,
            when (message) {
                is Throwable -> message.stackTraceToString()
                else -> message.toString()
            }
        )
    }

    fun writeText(level: Level, text: String)
}