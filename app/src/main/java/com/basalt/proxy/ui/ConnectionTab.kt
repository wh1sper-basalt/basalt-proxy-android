package com.basalt.proxy.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.basalt.proxy.ui.LocalIsBasaltTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.basalt.proxy.rememberHaptic
import com.basalt.proxy.BuildConfig
import com.basalt.proxy.ProxyController
import com.basalt.proxy.ProxyService
import com.basalt.proxy.SettingsStore
import com.basalt.proxy.R
import kotlinx.coroutines.launch


@Composable
fun ConnectionTab(settingsStore: SettingsStore) {
    val context = LocalContext.current
    val isRunning by ProxyService.isRunning.collectAsStateWithLifecycle()
    val isVerifiedRunning by ProxyService.isVerifiedRunning.collectAsStateWithLifecycle()
    val isReady by settingsStore.isReady.collectAsStateWithLifecycle(initialValue = false)

    val savedPort by settingsStore.port.collectAsStateWithLifecycle(initialValue = "1337")
    val savedBindIp by settingsStore.bindIp.collectAsStateWithLifecycle(initialValue = "127.0.0.1")
    val savedCfEnabled by settingsStore.cfproxyEnabled.collectAsStateWithLifecycle(initialValue = true)
    val savedPoolSize by settingsStore.poolSize.collectAsStateWithLifecycle(initialValue = 4)
    val savedSecretKey by settingsStore.secretKey.collectAsStateWithLifecycle(initialValue = "LOADING")
    val isBasaltTheme by settingsStore.isBasaltTheme.collectAsStateWithLifecycle(initialValue = false)

    val isBasaltFromLocal = LocalIsBasaltTheme.current
    val useBasalt = isBasaltTheme || isBasaltFromLocal
    val headerColor = if (useBasalt) Color.White else MaterialTheme.colorScheme.onSurface

    val scope = rememberCoroutineScope()
    val haptic = rememberHaptic(force = true)
    val currentVersion = remember { "v${BuildConfig.VERSION_NAME.removePrefix("v")}" }

    if (!isReady || savedSecretKey == "LOADING") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
        return
    }

    LaunchedEffect(savedSecretKey) {
        if (savedSecretKey == "") {
            val bytes = ByteArray(16)
            java.security.SecureRandom().nextBytes(bytes)
            val generated = bytes.joinToString("") { "%02x".format(it) }
            scope.launch { settingsStore.saveSecretKey(generated) }
        }
    }

    var isStarting by remember { mutableStateOf(false) }
    val statusText = when {
        isVerifiedRunning -> stringResource(R.string.status_connected)
        isStarting || isRunning -> stringResource(R.string.status_connecting)
        else -> stringResource(R.string.status_disconnected)
    }

    LaunchedEffect(isRunning, isVerifiedRunning) {
        if (isVerifiedRunning || !isRunning) isStarting = false
    }

    val port = savedPort.toIntOrNull() ?: 1337
    val secretForUrl = remember(savedSecretKey) {
        val raw = savedSecretKey.trim()
        if (raw.isNotEmpty() && raw != "LOADING") raw else "00000000000000000000000000000000"
    }
    val bindIp = savedBindIp.trim().takeIf { it.isNotEmpty() } ?: "127.0.0.1"
    val proxyUrl = "https://t.me/proxy?server=$bindIp&port=$port&secret=dd$secretForUrl"

    val connectAction = {
        if (!isRunning && !isStarting) {
            isStarting = true
            scope.launch {
                val started = ProxyController.startFromSavedSettings(context, true)
                if (!started) isStarting = false
            }
        }
    }
    val disconnectAction = { if (isRunning || isStarting) ProxyController.stop(context) }

    val isActiveVisual = isRunning || isStarting
    val logoScale by animateFloatAsState(
        targetValue = if (isActiveVisual) 1.06f else 0.94f,
        animationSpec = tween(600, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
        label = "logo_scale"
    )

    val basaltLogoScale by animateFloatAsState(
        targetValue = if (isActiveVisual) 1.18f else 1.05f,
        animationSpec = tween(600, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
        label = "basalt_logo_scale"
    )

    val logoInteractionSource = remember { MutableInteractionSource() }
    val statusColor by animateColorAsState(
        targetValue = if (isVerifiedRunning) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "status_color"
    )

    val primaryRaw = MaterialTheme.colorScheme.primary
    val surfaceVariantRaw = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantRaw = MaterialTheme.colorScheme.onSurfaceVariant

    val activeBg = if (primaryRaw.luminance() > 0.7f) {
        lerp(primaryRaw, Color.Black, 0.30f)
    } else {
        lerp(primaryRaw, MaterialTheme.colorScheme.primaryContainer, 0.20f)
    }


    val inactiveBg = if (surfaceVariantRaw.luminance() < 0.3f) {
        lerp(surfaceVariantRaw, onSurfaceVariantRaw, 0.30f)
    } else {
        lerp(surfaceVariantRaw, onSurfaceVariantRaw, 0.15f)
    }

    val logoBg by animateColorAsState(
        targetValue = if (isVerifiedRunning) activeBg else inactiveBg,
        animationSpec = tween(500),
        label = "logo_bg"
    )

    val planeTint = if (logoBg.luminance() > 0.65f) {
        Color(0xFF2A2A2A)
    } else {
        Color.White
    }

    val ringAlpha by animateFloatAsState(
        targetValue = if (isVerifiedRunning) 1f else 0f,
        animationSpec = tween(500),
        label = "ring_alpha"
    )
    val applyContainer by animateColorAsState(
        targetValue = if (isRunning) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        animationSpec = tween(280),
        label = "apply_container"
    )
    val applyContent by animateColorAsState(
        targetValue = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        animationSpec = tween(280),
        label = "apply_content"
    )

    var linkVisible by rememberSaveable { mutableStateOf(false) }

    val urlBlurRadius by animateDpAsState(
        targetValue = if (linkVisible) 0.dp else 14.dp,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "url_blur_radius"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.section_launch),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = headerColor
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AppSectionCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (useBasalt && isVerifiedRunning) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.40f * ringAlpha),
                                                Color.White.copy(alpha = 0.15f * ringAlpha),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }

                        if (!useBasalt && isVerifiedRunning) {
                            Box(
                                modifier = Modifier
                                    .size(190.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f * ringAlpha),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = logoInteractionSource,
                                    indication = null,
                                    onClick = {
                                        haptic()
                                        if (isActiveVisual) disconnectAction() else connectAction()
                                    }
                                )
                        ) {
                            if (useBasalt) {
                                val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.22f
                                Image(
                                    painter = painterResource(id = R.drawable.basalt_logo),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(basaltLogoScale),
                                    alpha = if (isDarkTheme) {
                                        if (isActiveVisual) 1f else 0.65f
                                    } else 1f
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(logoScale)
                                        .background(logoBg, CircleShape)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_telegram_plane),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().padding(20.dp),
                                        colorFilter = ColorFilter.tint(planeTint)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic()
                                applyToTelegramPackages(context, proxyUrl)
                            },
                            enabled = isRunning,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = applyContainer,
                                contentColor = applyContent
                            )
                        ) {
                            Text(
                                stringResource(R.string.apply_in_telegram),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        ProxyStatusPanel(
                            cfEnabled = savedCfEnabled,
                            poolSize = savedPoolSize,
                            port = savedPort,
                            version = currentVersion
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            // blur(0.dp) безопасен — если радиус 0, ничего не размывается
                                            Modifier.blur(
                                                urlBlurRadius,
                                                edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded
                                            )
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = proxyUrl,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.then(
                                            if (linkVisible) Modifier.horizontalScroll(rememberScrollState())
                                            else Modifier
                                        )
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    haptic()
                                    linkVisible = !linkVisible
                                },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (linkVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
                                        ),
                                        contentDescription = stringResource(
                                            if (linkVisible) R.string.hide_link else R.string.show_link
                                        ),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    haptic()
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    cb.setPrimaryClip(android.content.ClipData.newPlainText("Proxy", proxyUrl))
                                    Toast.makeText(context, context.resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.copy),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProxyStatusPanel(cfEnabled: Boolean, poolSize: Int, port: String, version: String) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProxyStatusItem(if (cfEnabled) "CF" else stringResource(R.string.direct_mode),
                Modifier.weight(0.9f).padding(horizontal = 6.dp))
            ProxyStatusDivider()
            ProxyStatusItem(stringResource(R.string.pool_short, poolSize),
                Modifier.weight(1.05f).padding(horizontal = 6.dp))
            ProxyStatusDivider()
            ProxyStatusItem(stringResource(R.string.port_short, port),
                Modifier.weight(1.35f).padding(horizontal = 6.dp))
            ProxyStatusDivider()
            ProxyStatusItem(version, Modifier.weight(1.1f).padding(horizontal = 6.dp))
        }
    }
}

@Composable
private fun ProxyStatusItem(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ProxyStatusDivider() {
    Box(Modifier.fillMaxHeight().width(1.dp)
        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)))
}

private val telegramPackages = listOf(
    "org.telegram.messenger", "com.radolyn.ayugram", "com.exteragram.messenger",
    "org.telegram.plus", "ir.ilmili.telegraph", "org.telegram.BifToGram",
    "tw.nekomimi.nekogram", "xyz.nextalone.nagram", "uz.unnarsx.cherrygram",
    "org.telegram.mdgram", "org.forkclient.messenger.beta", "app.nicegram",
    "top.qwq2333.nullgram", "com.iMe.android", "ru.dahl.messenger",
    "com.scriptsaz.litegram", "org.thunderdog.challegram"
)

private fun applyToTelegramPackages(context: Context, url: String) {
    val pm = context.packageManager
    val availablePackages = telegramPackages.filter {
        try { pm.getPackageInfo(it, 0); true } catch (_: PackageManager.NameNotFoundException) { false }
    }
    if (availablePackages.isEmpty()) {
        Toast.makeText(context, "Клиенты не найдены", Toast.LENGTH_SHORT).show()
        return
    }
    val intents = availablePackages.map { pkg -> Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { setPackage(pkg) } }
    if (intents.size == 1) {
        try { context.startActivity(intents.first().apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }
        catch (_: Exception) { Toast.makeText(context, "Ошибка при открытии клиента", Toast.LENGTH_SHORT).show() }
    } else {
        val chooser = Intent.createChooser(intents.first(), "Выберите клиент")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.drop(1).toTypedArray())
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { context.startActivity(chooser) }
        catch (_: Exception) { Toast.makeText(context, "Ошибка при выборе клиента", Toast.LENGTH_SHORT).show() }
    }
}