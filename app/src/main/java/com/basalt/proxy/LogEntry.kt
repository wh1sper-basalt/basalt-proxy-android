package com.basalt.proxy

import androidx.compose.runtime.Immutable


@Immutable
data class LogEntry(
    val key: String,
    val message: String,
    val count: Int,
    val isError: Boolean,
    val priority: Int, // 3=DEBUG, 4=INFO, 5=WARN, 6=ERROR
    val isEssential: Boolean = false
)
