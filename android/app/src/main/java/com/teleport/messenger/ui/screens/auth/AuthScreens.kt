package com.teleport.messenger.ui.screens.auth

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.theme.ManropeFontFamily
import com.teleport.messenger.viewmodel.TeleportViewModel

private const val PrefsName = "teleport_auth_ui"
private const val KeyRemember = "remember_me"
private const val KeyPhone = "remembered_phone"

@Composable
fun TeleportAuthScreen(vm: TeleportViewModel, onSuccess: () -> Unit) {
    var tab by remember { mutableStateOf(AuthTab.Login) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var agreed by remember { mutableStateOf(true) }
    var rememberMe by remember { mutableStateOf(false) }
    var showRecover by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
        rememberMe = prefs.getBoolean(KeyRemember, false)
        if (rememberMe) phone = prefs.getString(KeyPhone, "") ?: ""
    }

    LaunchedEffect(tab) { vm.clearError() }

    val normalizedPhone = normalizePhone(phone)
    val looksLikeUsername = phone.trim().startsWith("@") ||
        (phone.any { it.isLetter() } && !phone.trim().startsWith("+"))
    val loginValid = if (looksLikeUsername) {
        phone.removePrefix("@").trim().length >= 3 && password.length >= 8
    } else {
        normalizedPhone.length >= 12 && password.length >= 8
    }
    val registerValid = name.isNotBlank() && normalizedPhone.length >= 12 && password.length >= 8 && agreed

    val heroTitle = if (tab == AuthTab.Login) "С возвращением" else "Создай аккаунт"
    val heroSubtitle = if (tab == AuthTab.Login) {
        "Войдите, чтобы продолжить общение с друзьями"
    } else {
        "Общайся с друзьями и близкими в одном месте"
    }

    AuthScreenShell(heroTitle = heroTitle, heroSubtitle = heroSubtitle) {
        if (tab == AuthTab.Register) {
            AuthLabeledField(
                label = "Имя",
                value = name,
                onValueChange = { name = it },
                placeholder = "Как вас зовут",
                leadingIcon = { AuthFieldIcon(Icons.Outlined.Person) },
            )
        }

        AuthLabeledField(
                    label = "Номер телефона",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = if (tab == AuthTab.Login) "+7 … или @username" else "+7 900 000 00 00",
                    leadingIcon = { AuthFieldIcon(Icons.Outlined.Phone) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (tab == AuthTab.Login) KeyboardType.Text else KeyboardType.Phone,
                    ),
                )

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            AuthLabeledField(
                label = "Пароль",
                value = password,
                onValueChange = { password = it },
                placeholder = if (tab == AuthTab.Login) "Введите пароль" else "Минимум 8 символов",
                leadingIcon = { AuthFieldIcon(Icons.Outlined.Lock) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailing = {
                    IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = AuthPalette.TextDim,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
            )
            if (tab == AuthTab.Register) {
                AuthPasswordStrength(password)
            }
        }

        if (tab == AuthTab.Login) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(Modifier.weight(1f)) {
                    AuthCheckbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        label = {
                            Text(
                                "Запомнить меня",
                                fontFamily = ManropeFontFamily,
                                fontSize = 12.5.sp,
                                color = AuthPalette.TextDim,
                            )
                        },
                    )
                }
                Text(
                    "Забыли пароль?",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.5.sp,
                    color = AuthPalette.Link,
                    modifier = Modifier.clickable { showRecover = true },
                )
            }
        } else {
            AuthCheckbox(
                checked = agreed,
                onCheckedChange = { agreed = it },
                label = {
                    Text(
                        "Я согласен с условиями использования и политикой конфиденциальности",
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.5.sp,
                        color = AuthPalette.TextDim,
                        lineHeight = 17.sp,
                        modifier = Modifier.weight(1f),
                    )
                },
            )
        }

        error?.let {
            Text(
                it,
                color = androidx.compose.ui.graphics.Color(0xFFFF7A7A),
                fontFamily = ManropeFontFamily,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AuthPrimaryButton(
            text = if (tab == AuthTab.Login) "Войти" else "Зарегистрироваться",
            enabled = if (tab == AuthTab.Login) loginValid else registerValid,
            loading = loading,
        ) {
            context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).edit()
                .putBoolean(KeyRemember, rememberMe)
                .putString(KeyPhone, if (rememberMe) phone else "")
                .apply()

            if (tab == AuthTab.Login) {
                if (looksLikeUsername) {
                    vm.loginByUsername(phone.trim(), password, onSuccess)
                } else {
                    vm.login(normalizedPhone, password, onSuccess)
                }
            } else {
                vm.register(normalizedPhone, password, name.trim(), onSuccess)
            }
        }

        if (tab == AuthTab.Login) {
            val launchYandex = com.teleport.messenger.auth.rememberYandexAuthLauncher(
                onSuccess = { token -> vm.loginWithYandexToken(token, onSuccess) },
                onError = { msg -> vm.setError(msg) },
            )
            AuthFooterLink(
                prefix = "Или ",
                action = "войти через Яндекс ID",
            ) {
                launchYandex()
            }
        }

        AuthFooterLink(
            prefix = if (tab == AuthTab.Login) "Нет аккаунта? " else "Уже есть аккаунт? ",
            action = if (tab == AuthTab.Login) "Зарегистрироваться" else "Войти",
        ) {
            tab = if (tab == AuthTab.Login) AuthTab.Register else AuthTab.Login
            password = ""
        }
    }

    if (showRecover) {
        RecoverPasswordDialog(
            initialPhone = phone,
            onDismiss = { showRecover = false },
            onSubmit = { p, newPass ->
                vm.recover(normalizePhone(p), newPass) {
                    showRecover = false
                    phone = p
                    password = newPass
                    tab = AuthTab.Login
                }
            },
        )
    }
}

@Composable
private fun RecoverPasswordDialog(
    initialPhone: String,
    onDismiss: () -> Unit,
    onSubmit: (phone: String, newPassword: String) -> Unit,
) {
    var p by remember { mutableStateOf(initialPhone) }
    var np by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AuthPalette.Card,
        titleContentColor = AuthPalette.Text,
        textContentColor = AuthPalette.TextDim,
        title = {
            Text("Новый пароль", fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthLabeledField(
                    label = "Номер телефона",
                    value = p,
                    onValueChange = { p = it },
                    placeholder = "+7 900 000 00 00",
                    leadingIcon = { AuthFieldIcon(Icons.Outlined.Phone) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                AuthLabeledField(
                    label = "Новый пароль",
                    value = np,
                    onValueChange = { np = it },
                    placeholder = "Минимум 8 символов",
                    leadingIcon = { AuthFieldIcon(Icons.Outlined.Lock) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (normalizePhone(p).length >= 12 && np.length >= 8) onSubmit(p, np)
                },
            ) {
                Text("Сохранить", color = AuthPalette.Link, fontFamily = ManropeFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = AuthPalette.TextDim, fontFamily = ManropeFontFamily)
            }
        },
    )
}

/** Digits → +7XXXXXXXXXX for RU numbers. */
private fun normalizePhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    return when {
        digits.startsWith("8") && digits.length == 11 -> "+7${digits.drop(1)}"
        digits.startsWith("7") && digits.length == 11 -> "+$digits"
        digits.length == 10 -> "+7$digits"
        else -> if (raw.trim().startsWith("+")) "+$digits" else digits
    }
}

@Composable
fun UsernameLoginScreen(vm: TeleportViewModel, onSuccess: () -> Unit, onBack: (() -> Unit)? = null) =
    TeleportAuthScreen(vm, onSuccess)

@Composable
fun RegisterScreen(vm: TeleportViewModel, onSuccess: () -> Unit, onBack: () -> Unit) =
    TeleportAuthScreen(vm, onSuccess)

@Composable
fun PhoneAuthScreen(vm: TeleportViewModel, onBack: () -> Unit, onCodeSent: () -> Unit) =
    TeleportAuthScreen(vm, onSuccess = onCodeSent)
