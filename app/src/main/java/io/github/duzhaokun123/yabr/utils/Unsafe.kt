package io.github.duzhaokun123.yabr.utils

import com.ironz.unsafe.UnsafeAndroid
import io.github.duzhaokun123.yabr.logger.AndroidLogger

object Unsafe {
    private var _instance: UnsafeAndroid? = null
    val instance: UnsafeAndroid
        get() {
            if (_instance == null) {
                _instance = UnsafeAndroid()
//                val unsafe = _instance!!.getFieldValueAs<Any>("unsafe")
//                AndroidLogger.d("unsafe has following method(s)")
//                unsafe.javaClass.declaredMethods.forEach { method ->
//                    AndroidLogger.d(method)
//                }
            }
            return _instance!!
        }
}