package io.github.duzhaokun123.yabr.logger

interface Logger {
    enum class Level {
        DEBUG, INFO, WARN, ERROR, VERBOSE
    }

    fun d(vararg messages: Any?) {
        messages.forEach { message ->
            log(Level.DEBUG, message)
        }
    }

    fun i(vararg messages: Any?) {
        messages.forEach { message ->
            log(Level.INFO, message)
        }
    }

    fun w(vararg messages: Any?) {
        messages.forEach { message ->
            log(Level.WARN, message)
        }
    }

    fun e(vararg messages: Any?) {
        messages.forEach { message ->
            log(Level.ERROR, message)
        }
    }

    fun v(vararg messages: Any?) {
        messages.forEach { message ->
            log(Level.VERBOSE, message)
        }
    }

    fun log(level: Level, message: Any?) {
        writeText(
            level,
            when (message) {
                is Throwable -> message.stackTraceToString()
                is Array<*> -> message.contentToString()
                else -> message.toString()
            }
        )
    }

    fun writeText(level: Level, text: String)
}