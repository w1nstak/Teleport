package com.teleport.messenger.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.viewmodel.TeleportViewModel

@Composable
fun TeleportAuthScreen(vm: TeleportViewModel, onSuccess: () -> Unit) {
    var tab by remember { mutableStateOf(AuthTab.Register) }
    var nameValue by remember { mutableStateOf(TextFieldValue("")) }
    var usernameValue by remember { mutableStateOf(TextFieldValue("")) }
    var passwordValue by remember { mutableStateOf(TextFieldValue("")) }
    var showPassword by remember { mutableStateOf(false) }
    var usernameLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var fieldWidthPx by remember { mutableFloatStateOf(0f) }
    var usernameFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()

    val username = usernameValue.text
    val password = passwordValue.text
    val name = nameValue.text
    val loginValid = username.removePrefix("@").trim().length >= 3 && password.length >= 8
    val registerValid = name.isNotBlank() && username.removePrefix("@").length >= 3 && password.length >= 8
    val valid = if (tab == AuthTab.Login) loginValid else registerValid

    LaunchedEffect(tab) { vm.clearError() }

    AuthScreenShell {
        AuthWelcomeHeader()
        AuthTabSwitcher(tab) { tab = it }

        if (tab == AuthTab.Register) {
            AuthTextField(
                value = nameValue,
                onValueChange = { nameValue = it },
                placeholder = "Имя",
                leadingIcon = Icons.Outlined.Person,
            )
            Spacer(Modifier.height(10.dp))
        }

        Box(Modifier.fillMaxWidth()) {
            DolphinFieldPeek(
                text = username,
                cursorOffset = usernameValue.selection.start,
                textLayoutResult = usernameLayout,
                fieldWidthPx = fieldWidthPx,
                hideForPassword = passwordFocused,
                visible = usernameFocused || username.isNotEmpty(),
                modifier = Modifier.align(Alignment.TopStart),
            )
            AuthTextField(
                value = usernameValue,
                onValueChange = { v ->
                    val filtered = v.text.filter { c -> c.isLetterOrDigit() || c == '_' || c == '@' }
                    val sel = v.selection.start.coerceIn(0, filtered.length)
                    usernameValue = TextFieldValue(filtered, TextRange(sel))
                },
                placeholder = "Имя пользователя",
                leadingIcon = Icons.Outlined.AlternateEmail,
                onTextLayout = { usernameLayout = it },
                onWidthChanged = { fieldWidthPx = it },
                onFocusChanged = { usernameFocused = it },
                contentPaddingEnd = 52.dp,
            )
        }

        Spacer(Modifier.height(10.dp))

        AuthTextField(
            value = passwordValue,
            onValueChange = { passwordValue = it },
            placeholder = "Пароль",
            leadingIcon = Icons.Outlined.Lock,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            onFocusChanged = { passwordFocused = it },
            trailing = {
                IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = AuthPalette.IconTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )

        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(22.dp))

        AuthPrimaryButton(
            text = if (tab == AuthTab.Login) "Войти" else "Создать аккаунт",
            enabled = valid,
            loading = loading,
        ) {
            if (tab == AuthTab.Login) {
                vm.loginByUsername(username, password, onSuccess)
            } else {
                vm.registerByUsername(name, username, password, onSuccess)
            }
        }

        AuthSwitchModeLink(tab == AuthTab.Login) {
            tab = if (tab == AuthTab.Login) AuthTab.Register else AuthTab.Login
        }
        AuthPrivacyFooter()
    }
}

@Composable
private fun AuthTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onWidthChanged: (Float) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    contentPaddingEnd: androidx.compose.ui.unit.Dp = 0.dp,
    trailing: @Composable (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { onWidthChanged(it.width.toFloat()) }
            .height(54.dp)
            .clip(shape)
            .background(AuthPalette.InputBg)
            .border(1.dp, AuthPalette.InputBorder, shape)
            .padding(start = 14.dp, end = if (contentPaddingEnd > 0.dp) contentPaddingEnd else 14.dp)
            .onFocusChanged { onFocusChanged(it.isFocused) },
        textStyle = LocalTextStyle.current.copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = AuthPalette.TextPrimary,
        ),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        cursorBrush = SolidColor(AuthPalette.AccentCyan),
        decorationBox = { inner ->
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, null, tint = AuthPalette.IconTint, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.text.isEmpty()) {
                        Text(placeholder, color = AuthPalette.TextMuted.copy(0.65f), fontSize = 16.sp)
                    }
                    inner()
                }
                trailing?.invoke()
            }
        },
    )
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
