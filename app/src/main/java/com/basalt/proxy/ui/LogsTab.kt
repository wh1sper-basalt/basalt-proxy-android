package com.basalt.proxy.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basalt.proxy.LogEntry
import com.basalt.proxy.LogManager
import com.basalt.proxy.SettingsStore
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsTab(settingsStore: SettingsStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentLogs by LogManager.logs.collectAsStateWithLifecycle()

    val haptic = com.basalt.proxy.rememberHaptic()

    val savedInfo by settingsStore.logShowInfo.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_LOG_SHOW_INFO)
    val savedError by settingsStore.logShowError.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_LOG_SHOW_ERROR)
    val savedNull by settingsStore.logShowNull.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_LOG_SHOW_NULL)

    val isBasaltFromLocal = LocalIsBasaltTheme.current
    val headerColor = if (isBasaltFromLocal) Color.White else MaterialTheme.colorScheme.onSurface

    val nullLogsDisabled = stringResource(com.basalt.proxy.R.string.null_logs_disabled)
    val filteredLogs = remember(currentLogs, savedInfo, savedError, savedNull, nullLogsDisabled) {
        if (savedNull) {
            listOf(LogEntry(
                key = "null_msg",
                message = nullLogsDisabled,
                count = 1,
                isError = false,
                priority = 0,
                isEssential = false
            ))
        } else {
            currentLogs.filter { entry ->
                (savedInfo && entry.priority == 4) ||
                        (savedError && entry.priority >= 5)
            }
        }
    }

    val listState = rememberLazyListState()
    var hasInitialScrolled by remember { mutableStateOf(false) }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            if (!hasInitialScrolled) {
                listState.scrollToItem(filteredLogs.size - 1)
                hasInitialScrolled = true
            } else {
                listState.animateScrollToItem(filteredLogs.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(
                stringResource(com.basalt.proxy.R.string.event_log),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = headerColor,
                modifier = Modifier.align(Alignment.Center)
            )
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                IconButton(onClick = { LogManager.clearLogs() }) {
                    Icon(Icons.Default.Delete,
                        contentDescription = stringResource(com.basalt.proxy.R.string.clear),
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    val text = filteredLogs.joinToString("\n") { "${it.message} (x${it.count})" }
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("BasaltProxy Logs", text))
                    Toast.makeText(context, context.resources.getString(com.basalt.proxy.R.string.copied), Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy,
                        contentDescription = stringResource(com.basalt.proxy.R.string.copy),
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LogFilterChip("INFO", savedInfo && !savedNull, true, modifier = Modifier.weight(1f).height(56.dp)) {
                haptic()
                scope.launch {
                    val newInfo = if (savedNull) true else !savedInfo
                    val newError = if (savedNull) false else savedError
                    val newNull = !newInfo && !newError
                    settingsStore.saveLogFilters(false, newInfo, newError, newNull)
                }
            }
            LogFilterChip("ERROR", savedError && !savedNull, true, modifier = Modifier.weight(1f).height(56.dp)) {
                haptic()
                scope.launch {
                    val newError = if (savedNull) true else !savedError
                    val newInfo = if (savedNull) false else savedInfo
                    val newNull = !newInfo && !newError
                    settingsStore.saveLogFilters(false, newInfo, newError, newNull)
                }
            }
            LogFilterChip("NULL", savedNull, true, modifier = Modifier.weight(1f).height(56.dp)) {
                haptic()
                scope.launch {
                    settingsStore.saveLogFilters(false, false, false, true)
                }
            }
        }

        val terminalBg = AppColors.terminalBg(MaterialTheme.colorScheme)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(terminalBg)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(items = filteredLogs, key = { it.key }) { entry ->
                    LogLine(entry)
                }
            }
        }
    }
}

@Composable
private fun LogFilterChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(220),
        label = "chip_container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(220),
        label = "chip_content"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun LogLine(entry: LogEntry) {
    val color = when (entry.priority) {
        0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) // NULL — нейтральный
        6 -> AppColors.terminalRed
        5 -> AppColors.terminalOrange
        4 -> AppColors.terminalGreen
        3 -> AppColors.terminalBlue
        else -> AppColors.terminalText
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = AppColors.terminalCounter.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 5.dp)
            ) {
                Text("${entry.count}", color = AppColors.terminalBlue,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        val icon = when (entry.priority) {
            0 -> null
            6 -> Icons.Default.Error
            5 -> Icons.Default.Warning
            4 -> Icons.Default.Info
            3 -> Icons.Default.BugReport
            else -> Icons.Default.Info
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(entry.message, color = color, fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (entry.isError) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 17.sp, modifier = Modifier.weight(1f))
    }
}