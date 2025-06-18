package io.github.duzhaokun123.yabr.utils

class UnresearchableError : Error()

fun Unresearchable(): Nothing = throw UnresearchableError()