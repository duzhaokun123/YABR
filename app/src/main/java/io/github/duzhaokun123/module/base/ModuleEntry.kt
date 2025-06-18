package io.github.duzhaokun123.module.base

@Target(AnnotationTarget.CLASS)
annotation class ModuleEntry(
    val id: String,
    val targets: Array<String> = [],
    val priority: Int = 10,
)
