package io.github.duzhaokun123.yabr.utils

import android.os.Build
import java.io.InputStream

fun getResId(name: String): Int {
    val context = loaderContext.application
    return context.resources.getIdentifier(name, "id", context.packageName)
}

val Number.dp: Int
    get() = (this.toFloat() * loaderContext.application.resources.displayMetrics.density).toInt()

fun InputStream.readAll(): ByteArray {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readAllBytes()
    } else {
        TODO("VERSION.SDK_INT < TIRAMISU")
    }
}

val Throwable.reason: String
    get() = localizedMessage ?: message ?: toString()


fun runNewThread(
    name: String? = null,
    block: () -> Unit
) {
    Thread(block, name ?: "NewThread-${System.currentTimeMillis()}").start()
}

fun runMainThread(
    block: () -> Unit
) {
    Toast.handler.post(block)
}
