package com.teleport.messenger.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.R
import com.teleport.messenger.viewmodel.TeleportViewModel
import com.teleport.messenger.ui.theme.TeleportAppTheme

private val MaxButtonBg @Composable get() = TeleportAppTheme.colors.accentBlue
private val MaxButtonDisabled @Composable get() = TeleportAppTheme.colors.accentBlue.copy(alpha = 0.35f)
private val MaxBrandColor @Composable get() = TeleportAppTheme.colors.accentBlue
private val MaxScreenBg @Composable get() = TeleportAppTheme.colors.screenBg
private val MaxInputBg @Composable get() = TeleportAppTheme.colors.authInputBg
private val MaxTextPrimary @Composable get() = TeleportAppTheme.colors.authTextPrimary
private val MaxTextSecondary @Composable get() = TeleportAppTheme.colors.authTextSecondary

@Composable
fun UsernameLoginScreen(
    vm: TeleportViewModel,
    onSuccess: () -> Unit,
    onBack: (() -> Unit)? = null,
    showHelp: Boolean = false,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()
    val valid = username.removePrefix("@").trim().length >= 3 && password.length >= 8

    LaunchedEffect(Unit) { vm.clearError() }

    MaxAuthScaffold(onBack = onBack, showHelp = showHelp) {
        MaxBrandRow(compact = onBack != null)
        Spacer(Modifier.height(if (onBack != null) 32.dp else 40.dp))
        MaxTitleBlock(
            title = "Вход",
            subtitle = "Введите @username и пароль",
        )
        Spacer(Modifier.height(24.dp))
        MaxTextField(
            value = username,
            onValueChange = { username = it.filter { c -> c.isLetterOrDigit() || c == '_' || c == '@' } },
            placeholder = "@username",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        Spacer(Modifier.height(12.dp))
        MaxTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Пароль",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailing = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Скрыть" else "Показать", fontSize = 12.sp, color = MaxTextSecondary)
                }
            },
        )
        Text(
            "Не менее 8 символов",
            fontSize = 13.sp,
            color = MaxTextSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        MaxPrimaryButton(
            text = "Войти",
            enabled = valid && !loading,
            loading = loading,
            onClick = { vm.loginByUsername(username, password, onSuccess) },
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MaxAuthScaffold(
    onBack: (() -> Unit)? = null,
    showHelp: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaxScreenBg),
    ) {
        if (showHelp) {
            MaxHelpButton(Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 16.dp))
        }
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 20.dp),
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = MaxTextPrimary)
                }
            } else {
                Spacer(Modifier.height(48.dp))
            }
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                content = content,
            )
        }
    }
}

@Composable
private fun MaxHelpButton(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
            .clickable { },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.HelpOutline, null, tint = MaxTextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun MaxBrandRow(compact: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(if (compact) 36.dp else 44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaxBrandColor),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_teleport_logo),
                contentDescription = null,
                modifier = Modifier.size(if (compact) 22.dp else 24.dp),
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "Teleport",
            fontSize = if (compact) 22.sp else 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaxTextPrimary,
            letterSpacing = (-0.3).sp,
        )
    }
}

@Composable
private fun MaxTitleBlock(title: String, subtitle: String) {
    Text(
        title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaxTextPrimary,
        lineHeight = 30.sp,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        subtitle,
        fontSize = 15.sp,
        color = MaxTextSecondary,
        lineHeight = 20.sp,
    )
}

@Composable
private fun MaxPrimaryButton(
    text: String,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaxButtonBg,
            disabledContainerColor = MaxButtonDisabled,
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.85f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MaxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaxInputBg)
            .padding(horizontal = 18.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = MaxTextPrimary),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(placeholder, color = MaxTextSecondary.copy(alpha = 0.55f), fontSize = 16.sp)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { inner() }
                    trailing?.invoke()
                }
            }
        },
    )
}

private val WelcomeBg = Color(0xFFF2F2F7)
private val WelcomeText = Color(0xFF000000)
private val WelcomeMuted = Color(0xFF8E8E93)
private val WelcomeSecondaryBtn = Color(0xFFE5E5EA)

@Composable
fun WelcomeAuthScreen(
    onPhone: () -> Unit,
    onUsername: () -> Unit,
    onRegister: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(WelcomeBg)) {
        listOf(0.12f to 0.18f, 0.72f to 0.08f, 0.45f to 0.55f, 0.85f to 0.35f).forEach { (x, y) ->
            Box(
                Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
                    .offset(x = (x * 320).dp, y = (y * 600).dp)
                    .size((40 + (x * 80)).dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.04f)),
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Outlined.ChatBubbleOutline,
                null,
                modifier = Modifier.size(56.dp),
                tint = WelcomeText,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                "Простой, Быстрый\nмессенджер",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = WelcomeText,
                lineHeight = 32.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onPhone,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WelcomeText),
            ) {
                Text("Войти / Регистрация по номеру", color = Color.White, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onUsername,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WelcomeSecondaryBtn),
            ) {
                Text("Войти по юзернейму", color = WelcomeText, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WelcomeSecondaryBtn),
            ) {
                Text("Зарегистрироваться без номера", color = WelcomeText, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RegisterScreen(vm: TeleportViewModel, onSuccess: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()
    val valid = name.isNotBlank() && username.removePrefix("@").length >= 3 && password.length >= 8

    MaxAuthScaffold(onBack = onBack) {
        MaxTitleBlock("Регистрация", "Без номера телефона")
        Spacer(Modifier.height(24.dp))
        MaxTextField(name, { name = it }, "Имя")
        Spacer(Modifier.height(12.dp))
        MaxTextField(
            username,
            { username = it.filter { c -> c.isLetterOrDigit() || c == '_' || c == '@' } },
            "@username",
        )
        Spacer(Modifier.height(12.dp))
        MaxTextField(
            password,
            { password = it },
            "Пароль",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp)) }
        Spacer(Modifier.weight(1f))
        MaxPrimaryButton(
            "Создать аккаунт",
            valid && !loading,
            loading,
        ) { vm.registerByUsername(name, username, password, onSuccess) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun PhoneAuthScreen(vm: TeleportViewModel, onBack: () -> Unit, onCodeSent: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    val error by vm.error.collectAsState()
    MaxAuthScaffold(onBack = onBack) {
        MaxTitleBlock("По номеру", "Введите номер телефона")
        Spacer(Modifier.height(24.dp))
        MaxTextField(phone, { phone = it.filter { it.isDigit() || it == '+' } }, "+7...")
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp)) }
        Spacer(Modifier.weight(1f))
        MaxPrimaryButton("Продолжить", phone.length >= 10) {
            vm.sendPhoneCode("+${phone.trimStart('+')}", false, onSent = onCodeSent)
        }
        Spacer(Modifier.height(16.dp))
    }
}
