package com.teleport.messenger.util

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.teleport.messenger.ui.components.TeleportButton
import com.teleport.messenger.ui.components.TeleportTextField
import java.security.MessageDigest

@Composable
fun AppLockGate(
    enabled: Boolean,
    biometricEnabled: Boolean,
    pinHash: String?,
    content: @Composable () -> Unit,
) {
    var unlocked by remember { mutableStateOf(!enabled || pinHash.isNullOrBlank()) }
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(enabled) {
        if (!enabled || pinHash.isNullOrBlank()) unlocked = true
        else if (biometricEnabled && context is FragmentActivity) {
            BiometricHelper.authenticate(context, onSuccess = { unlocked = true }, onError = {})
        }
    }

    if (unlocked) {
        content()
    } else {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Teleport заблокирован", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            TeleportTextField(pin, { pin = it; pinError = false }, "PIN-код")
            if (pinError) {
                Spacer(Modifier.height(8.dp))
                Text("Неверный PIN", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
            TeleportButton("Разблокировать", {
                if (hashPin(pin) == pinHash) {
                    unlocked = true
                    pinError = false
                } else {
                    pinError = true
                }
            })
            if (biometricEnabled && context is FragmentActivity) {
                TextButton(onClick = { BiometricHelper.authenticate(context, { unlocked = true }) {} }) {
                    Text("Биометрия")
                }
            }
        }
    }
}

fun hashPin(pin: String): String =
    MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        .joinToString("") { "%02x".format(it) }
