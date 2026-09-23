package com.basalt.proxy.ui

import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basalt.proxy.ProxyService
import com.basalt.proxy.R
import com.basalt.proxy.SettingsStore
import com.basalt.proxy.VibrationHelper
import com.basalt.proxy.rememberHaptic
import com.basalt.proxy.ui.LocalIsBasaltTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private fun generateRandomSecret(): String {
    val bytes = ByteArray(16)
    java.security.SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

fun openTelegram(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.telegram_not_found, Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(settingsStore: SettingsStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isRunning by ProxyService.isRunning.collectAsStateWithLifecycle()
    val haptic = rememberHaptic()

    val isReady by settingsStore.isReady.collectAsStateWithLifecycle(initialValue = false)
    val isExperimental by settingsStore.isExperimentalMode.collectAsStateWithLifecycle(initialValue = false)

    val savedIsDcAuto by settingsStore.isDcAuto.collectAsStateWithLifecycle(initialValue = true)
    val savedDc1 by settingsStore.dc1.collectAsStateWithLifecycle(initialValue = "")
    val savedDc2 by settingsStore.dc2.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_DIRECT_DC2_IP)
    val savedDc3 by settingsStore.dc3.collectAsStateWithLifecycle(initialValue = "")
    val savedDc4 by settingsStore.dc4.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_DIRECT_DC4_IP)
    val savedDc5 by settingsStore.dc5.collectAsStateWithLifecycle(initialValue = "")
    val savedDc203 by settingsStore.dc203.collectAsStateWithLifecycle(initialValue = "")
    val savedDc1m by settingsStore.dc1m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc2m by settingsStore.dc2m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc3m by settingsStore.dc3m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc4m by settingsStore.dc4m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc5m by settingsStore.dc5m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc203m by settingsStore.dc203m.collectAsStateWithLifecycle(initialValue = "")
    val savedPort by settingsStore.port.collectAsStateWithLifecycle(initialValue = "1337")
    val savedBindIp by settingsStore.bindIp.collectAsStateWithLifecycle(initialValue = "127.0.0.1")
    val savedPoolSize by settingsStore.poolSize.collectAsStateWithLifecycle(initialValue = 4)
    val savedCfEnabled by settingsStore.cfproxyEnabled.collectAsStateWithLifecycle(initialValue = true)
    val savedCustomDomainEnabled by settingsStore.customCfDomainEnabled.collectAsStateWithLifecycle(initialValue = false)
    val savedCustomDomain by settingsStore.customCfDomain.collectAsStateWithLifecycle(initialValue = "")
    val autoStartOnBoot by settingsStore.autoStartOnBoot.collectAsStateWithLifecycle(initialValue = false)
    val savedSecretKey by settingsStore.secretKey.collectAsStateWithLifecycle(initialValue = "LOADING")

    val themeMode by settingsStore.themeMode.collectAsStateWithLifecycle(initialValue = "system")
    val isDynamicColor by settingsStore.isDynamicColor.collectAsStateWithLifecycle(initialValue = true)
    val themePalette by settingsStore.themePalette.collectAsStateWithLifecycle(initialValue = "indigo")
    val isBasaltTheme by settingsStore.isBasaltTheme.collectAsStateWithLifecycle(initialValue = false)

    val isBasaltFromLocal = LocalIsBasaltTheme.current
    val useBasalt = isBasaltTheme || isBasaltFromLocal
    val headerColor = if (useBasalt) Color.White else MaterialTheme.colorScheme.onSurface

    val vibrationEnabled by settingsStore.vibrationEnabled.collectAsStateWithLifecycle(initialValue = false)
    val vibrationStrength by settingsStore.vibrationStrength.collectAsStateWithLifecycle(initialValue = 5)
    val vibrationMode by settingsStore.vibrationMode.collectAsStateWithLifecycle(initialValue = "all")

    if (!isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
        return
    }

    var isDcAuto by rememberSaveable(savedIsDcAuto) { mutableStateOf(savedIsDcAuto) }
    var experimentalMode by rememberSaveable(isExperimental) { mutableStateOf(isExperimental) }
    var dc1Text by rememberSaveable(savedDc1) { mutableStateOf(savedDc1) }
    var dc2Text by rememberSaveable(savedDc2) { mutableStateOf(savedDc2) }
    var dc3Text by rememberSaveable(savedDc3) { mutableStateOf(savedDc3) }
    var dc4Text by rememberSaveable(savedDc4) { mutableStateOf(savedDc4) }
    var dc5Text by rememberSaveable(savedDc5) { mutableStateOf(savedDc5) }
    var dc203Text by rememberSaveable(savedDc203) { mutableStateOf(savedDc203) }
    var dc1mText by rememberSaveable(savedDc1m) { mutableStateOf(savedDc1m) }
    var dc2mText by rememberSaveable(savedDc2m) { mutableStateOf(savedDc2m) }
    var dc3mText by rememberSaveable(savedDc3m) { mutableStateOf(savedDc3m) }
    var dc4mText by rememberSaveable(savedDc4m) { mutableStateOf(savedDc4m) }
    var dc5mText by rememberSaveable(savedDc5m) { mutableStateOf(savedDc5m) }
    var dc203mText by rememberSaveable(savedDc203m) { mutableStateOf(savedDc203m) }
    var portText by rememberSaveable(savedPort) { mutableStateOf(savedPort) }
    var bindIpText by rememberSaveable(savedBindIp) { mutableStateOf(savedBindIp) }
    var selectedPoolSize by rememberSaveable(savedPoolSize) { mutableIntStateOf(savedPoolSize) }
    var cfEnabled by rememberSaveable(savedCfEnabled) { mutableStateOf(savedCfEnabled) }
    var customCfDomainEnabled by rememberSaveable(savedCustomDomainEnabled) { mutableStateOf(savedCustomDomainEnabled) }
    var customCfDomain by rememberSaveable(savedCustomDomain) { mutableStateOf(savedCustomDomain) }
    var secretKeyText by remember(savedSecretKey) { mutableStateOf(if (savedSecretKey == "LOADING") "" else savedSecretKey) }

    LaunchedEffect(savedSecretKey) {
        if (savedSecretKey == "") {
            val generated = generateRandomSecret()
            secretKeyText = generated
            settingsStore.saveSecretKey(generated)
        } else if (savedSecretKey != "LOADING") {
            secretKeyText = savedSecretKey
        }
    }

    var saveJob by remember { mutableStateOf<Job?>(null) }
    fun scheduleSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(300)
            settingsStore.saveAll(
                isDcAuto, dc1Text, dc2Text, dc3Text, dc4Text, dc5Text, dc203Text,
                dc1mText, dc2mText, dc3mText, dc4mText, dc5mText, dc203mText,
                experimentalMode, bindIpText, portText, selectedPoolSize,
                cfEnabled, customCfDomainEnabled, customCfDomain, secretKeyText
            )
        }
    }

    var showIpSetupDialog by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val paletteVisible = !isBasaltTheme && !(isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    var initialPalette by remember { mutableStateOf(true) }
    LaunchedEffect(paletteVisible) {
        if (initialPalette) { initialPalette = false; return@LaunchedEffect }
        if (paletteVisible) {
            delay(340)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    var initialVibration by remember { mutableStateOf(true) }
    LaunchedEffect(vibrationEnabled) {
        if (initialVibration) { initialVibration = false; return@LaunchedEffect }
        if (vibrationEnabled) {
            delay(340)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    fun switchLanguage(lang: String, bgColor: Int) {
        val prefs = context.getSharedPreferences("lang_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", lang).apply()

        val act = context as? android.app.Activity ?: return

        try {
            act.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgColor))
        } catch (_: Throwable) {}

        com.basalt.proxy.LanguageSwitchState.setSwitching(true)
        prefs.edit().putBoolean("language_switching_pending", true).apply()

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            val applied: Boolean = try {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    androidx.core.os.LocaleListCompat.forLanguageTags(lang)
                )
                // Проверяем, реально ли применилось
                val actual = androidx.appcompat.app.AppCompatDelegate
                    .getApplicationLocales().toLanguageTags()
                actual.startsWith(lang, ignoreCase = true)
            } catch (_: Throwable) {
                false
            }

            if (applied) {
                try {
                    val svcIntent = Intent(context, com.basalt.proxy.ProxyService::class.java).apply {
                        action = com.basalt.proxy.ProxyService.ACTION_REFRESH_NOTIFICATION
                    }
                    context.startService(svcIntent)
                } catch (_: Throwable) {}

                handler.postDelayed({
                    com.basalt.proxy.LanguageSwitchState.setSwitching(false)
                    prefs.edit().putBoolean("language_switching_pending", false).apply()
                }, 400)
            } else {
                if (Build.VERSION.SDK_INT >= 34) {
                    act.overrideActivityTransition(
                        android.app.Activity.OVERRIDE_TRANSITION_CLOSE,
                        R.anim.fade_out, R.anim.fade_in
                    )
                }
                act.recreate()
                if (Build.VERSION.SDK_INT in 16 until 34) {
                    @Suppress("DEPRECATION")
                    act.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            }
        }, 320)
    }

    if (showIpSetupDialog) {
        IpSetupDialog(
            isExperimental = experimentalMode,
            onExperimentalChange = { experimentalMode = it; scheduleSave() },
            dc1Text = dc1Text, onDc1Change = { dc1Text = it; scheduleSave() },
            dc2Text = dc2Text, onDc2Change = { dc2Text = it; scheduleSave() },
            dc3Text = dc3Text, onDc3Change = { dc3Text = it; scheduleSave() },
            dc4Text = dc4Text, onDc4Change = { dc4Text = it; scheduleSave() },
            dc5Text = dc5Text, onDc5Change = { dc5Text = it; scheduleSave() },
            dc203Text = dc203Text, onDc203Change = { dc203Text = it; scheduleSave() },
            dc1mText = dc1mText, onDc1mChange = { dc1mText = it; scheduleSave() },
            dc2mText = dc2mText, onDc2mChange = { dc2mText = it; scheduleSave() },
            dc3mText = dc3mText, onDc3mChange = { dc3mText = it; scheduleSave() },
            dc4mText = dc4mText, onDc4mChange = { dc4mText = it; scheduleSave() },
            dc5mText = dc5mText, onDc5mChange = { dc5mText = it; scheduleSave() },
            dc203mText = dc203mText, onDc203mChange = { dc203mText = it; scheduleSave() },
            onDismiss = { showIpSetupDialog = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.settings),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = headerColor)
        }

        Spacer(Modifier.height(8.dp))

        AppSectionCard {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Public, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.connection), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Text(stringResource(R.string.ip_and_port_label),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    color = Color.Transparent
                ) {
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bindIpText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ),
                                maxLines = 1
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        androidx.compose.foundation.text.BasicTextField(
                            value = portText,
                            onValueChange = { val f = it.filter { c -> c.isDigit() }
                                if (f.length <= 5) { portText = f; scheduleSave() } },
                            modifier = Modifier.width(140.dp).padding(horizontal = 8.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        haptic()
                        showIpSetupDialog = true
                    },
                    enabled = !cfEnabled && !isRunning,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(
                        alpha = if (cfEnabled || isRunning) 0.2f else 0.5f))
                ) {
                    Icon(Icons.Default.Settings, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(if (cfEnabled) R.string.auto_cf_enabled else R.string.configure_dc_addresses),
                        fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Layers, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.ws_pool), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 4, 6).forEach { size ->
                        AnimatedChip(
                            label = "$size",
                            selected = selectedPoolSize == size,
                            enabled = !isRunning,
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { haptic(); selectedPoolSize = size; scheduleSave() }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.VpnKey, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.secret_key), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                OutlinedTextField(
                    value = secretKeyText,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                haptic()
                                val newKey = generateRandomSecret()
                                secretKeyText = newKey
                                scope.launch { settingsStore.saveSecretKey(newKey) }
                                scheduleSave()
                            },
                            enabled = !isRunning
                        ) { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Cloud, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text("CloudFlare CDN", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Switch(
                    checked = cfEnabled,
                    onCheckedChange = { haptic(); cfEnabled = it; isDcAuto = it; scheduleSave() },
                    enabled = !isRunning
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PowerSettingsNew, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.autostart), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Switch(checked = autoStartOnBoot, onCheckedChange = {
                    haptic()
                    scope.launch { settingsStore.saveAutoStartOnBoot(it) }
                })
            }
        }

        Spacer(Modifier.height(12.dp))

        AppSectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.ic_palette), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedChip(stringResource(R.string.theme_system), themeMode == "system", modifier = Modifier.weight(1f).height(48.dp)) {
                    haptic(); scope.launch { settingsStore.saveThemeMode("system") }
                }
                AnimatedChip(stringResource(R.string.theme_light), themeMode == "light", modifier = Modifier.weight(1f).height(48.dp)) {
                    haptic(); scope.launch { settingsStore.saveThemeMode("light") }
                }
                AnimatedChip(stringResource(R.string.theme_dark), themeMode == "dark", modifier = Modifier.weight(1f).height(48.dp)) {
                    haptic(); scope.launch { settingsStore.saveThemeMode("dark") }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.basalt_theme_title),
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = isBasaltTheme, onCheckedChange = {
                    haptic()
                    scope.launch { settingsStore.saveBasaltTheme(it) }
                })
            }

            AnimatedVisibility(
                visible = !isBasaltTheme,
                enter = expandVertically(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(220)),
                exit = shrinkVertically(tween(280, easing = FastOutSlowInEasing)) + fadeOut(tween(180))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.dynamic_colors),
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                            onCheckedChange = {
                                haptic()
                                scope.launch { settingsStore.saveDynamicColor(it) }
                            },
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        )
                    }

                    AnimatedVisibility(
                        visible = !(isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S),
                        enter = expandVertically(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(220)),
                        exit = shrinkVertically(tween(280, easing = FastOutSlowInEasing)) + fadeOut(tween(180))
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            Text(stringResource(R.string.palette),
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PaletteCircle("indigo", 0xFF5B588D, themePalette) { p ->
                                    haptic()
                                    scope.launch { settingsStore.saveThemePalette(p) }
                                }
                                PaletteCircle("forest", 0xFF5F5D68, themePalette) { p ->
                                    haptic()
                                    scope.launch { settingsStore.saveThemePalette(p) }
                                }
                                PaletteCircle("espresso", 0xFF6D4C41, themePalette) { p ->
                                    haptic()
                                    scope.launch { settingsStore.saveThemePalette(p) }
                                }
                                PaletteCircle("pink", 0xFFE5A3B8, themePalette) { p ->
                                    haptic()
                                    scope.launch { settingsStore.saveThemePalette(p) }
                                }
                                PaletteCircle("mono", 0xFF6E6E6E, themePalette) { p ->
                                    haptic()
                                    scope.launch { settingsStore.saveThemePalette(p) }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        AppSectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.system_settings), style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }

            Text("Language / Язык", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            val prefs = context.getSharedPreferences("lang_prefs", Context.MODE_PRIVATE)
            var currentLang by remember { mutableStateOf(prefs.getString("app_language", "ru") ?: "ru") }
            val bgColorInt = MaterialTheme.colorScheme.background.toArgb()

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedChip("Русский", currentLang == "ru", modifier = Modifier.weight(1f).height(48.dp)) {
                    haptic()
                    currentLang = "ru"
                    switchLanguage("ru", bgColorInt)
                }
                AnimatedChip("English", currentLang == "en", modifier = Modifier.weight(1f).height(48.dp)) {
                    haptic()
                    currentLang = "en"
                    switchLanguage("en", bgColorInt)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.vibration_feedback),
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = vibrationEnabled, onCheckedChange = {
                    haptic()
                    scope.launch { settingsStore.saveVibrationEnabled(it) }
                })
            }

            AnimatedVisibility(
                visible = vibrationEnabled,
                enter = expandVertically(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(220)),
                exit = shrinkVertically(tween(280, easing = FastOutSlowInEasing)) + fadeOut(tween(180))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    Text(stringResource(R.string.vibration_strength),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    var lastPreviewed by remember { mutableIntStateOf(vibrationStrength) }
                    Slider(
                        value = vibrationStrength.toFloat(),
                        onValueChange = { newValue ->
                            val newInt = newValue.toInt()
                            scope.launch { settingsStore.saveVibrationStrength(newInt) }
                            if (newInt != lastPreviewed) {
                                lastPreviewed = newInt
                                VibrationHelper.vibrate(context, newInt)
                            }
                        },
                        valueRange = 0f..10f,
                        steps = 9
                    )

                    Text(stringResource(R.string.vibration_mode),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnimatedChip(stringResource(R.string.vibration_all_buttons),
                            vibrationMode == "all", modifier = Modifier.weight(1f).height(48.dp)) {
                            haptic()
                            scope.launch { settingsStore.saveVibrationMode("all") }
                        }
                        AnimatedChip(stringResource(R.string.vibration_power_only),
                            vibrationMode == "power_only", modifier = Modifier.weight(1f).height(48.dp)) {
                            haptic()
                            scope.launch { settingsStore.saveVibrationMode("power_only") }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PaletteCircle(
    paletteId: String,
    colorHex: Long,
    selectedId: String,
    onClick: (String) -> Unit
) {
    val isSelected = paletteId == selectedId
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(colorHex))
            .clickable { onClick(paletteId) }
            .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IpSetupDialog(
    isExperimental: Boolean, onExperimentalChange: (Boolean) -> Unit,
    dc1Text: String, onDc1Change: (String) -> Unit,
    dc2Text: String, onDc2Change: (String) -> Unit,
    dc3Text: String, onDc3Change: (String) -> Unit,
    dc4Text: String, onDc4Change: (String) -> Unit,
    dc5Text: String, onDc5Change: (String) -> Unit,
    dc203Text: String, onDc203Change: (String) -> Unit,
    dc1mText: String, onDc1mChange: (String) -> Unit,
    dc2mText: String, onDc2mChange: (String) -> Unit,
    dc3mText: String, onDc3mChange: (String) -> Unit,
    dc4mText: String, onDc4mChange: (String) -> Unit,
    dc5mText: String, onDc5mChange: (String) -> Unit,
    dc203mText: String, onDc203mChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val onIpChange = { v: String, u: (String) -> Unit -> u(v.filter { it.isDigit() || it == '.' }) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight().heightIn(max = 560.dp)) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.dc_addresses), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                @Composable
                fun dcInput(label: String, value: String, update: (String) -> Unit) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(label, style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = value, onValueChange = { onIpChange(it, update) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true, modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    }
                }

                Column(Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isExperimental) {
                        dcInput("DC1", dc1Text, onDc1Change); dcInput("DC2", dc2Text, onDc2Change)
                        dcInput("DC3", dc3Text, onDc3Change); dcInput("DC4", dc4Text, onDc4Change)
                        dcInput("DC5", dc5Text, onDc5Change); dcInput("DC203", dc203Text, onDc203Change)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(stringResource(R.string.media_dcs), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        dcInput("DC1m", dc1mText, onDc1mChange); dcInput("DC2m", dc2mText, onDc2mChange)
                        dcInput("DC3m", dc3mText, onDc3mChange); dcInput("DC4m", dc4mText, onDc4mChange)
                        dcInput("DC5m", dc5mText, onDc5mChange); dcInput("DC203m", dc203mText, onDc203mChange)
                    } else {
                        dcInput("DC2", dc2Text, onDc2Change); dcInput("DC4", dc4Text, onDc4Change)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.experimental_mode), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Switch(checked = isExperimental, onCheckedChange = onExperimentalChange)
                }

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp)) {
                    Text(stringResource(R.string.done), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}