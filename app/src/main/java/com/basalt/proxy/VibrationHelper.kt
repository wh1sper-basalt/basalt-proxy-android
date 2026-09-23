package com.basalt.proxy

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle


object VibrationHelper {
    fun vibrate(context: Context, strength: Int) {
        if (strength <= 0) return
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        val amplitude = (strength.coerceIn(1, 10) * 24) + 15  // 39..255
        val duration = 10L + strength * 2L                     // 12..30 ms

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, amplitude.coerceIn(1, 255))
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val m = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            m?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

@Composable
fun rememberHaptic(force: Boolean = false): () -> Unit {
    val context = LocalContext.current
    val settingsStore = remember { SettingsStore(context) }
    val enabled by settingsStore.vibrationEnabled.collectAsStateWithLifecycle(initialValue = false)
    val strength by settingsStore.vibrationStrength.collectAsStateWithLifecycle(initialValue = 5)
    val mode by settingsStore.vibrationMode.collectAsStateWithLifecycle(initialValue = "all")

    return remember(enabled, strength, mode, force) {
        {
            if (enabled && (mode == "all" || force)) {
                VibrationHelper.vibrate(context, strength)
            }
        }
    }
}