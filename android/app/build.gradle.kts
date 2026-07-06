import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}
fun resolveYandexClientId(): String {
    localProperties.getProperty("yandex.client.id", "").trim().takeIf { it.isNotEmpty() }?.let { return it }
    val idFile = rootProject.file("yandex.client.id")
    if (idFile.exists()) {
        idFile.readText().trim().takeIf { it.isNotEmpty() }?.let { return it }
    }
    return System.getenv("YANDEX_CLIENT_ID")?.trim().orEmpty()
}
val yandexClientId: String = resolveYandexClientId()
fun resolveTelegramBotToken(): String {
    localProperties.getProperty("telegram.bot.token", "").trim().takeIf { it.isNotEmpty() }?.let { return it }
    val file = rootProject.file("telegram.bot.token")
    if (file.exists()) file.readText().trim().takeIf { it.isNotEmpty() }?.let { return it }
    return System.getenv("TELEGRAM_BOT_TOKEN")?.trim().orEmpty()
}
val telegramBotToken: String = resolveTelegramBotToken()
val telegramBotUsername: String = localProperties.getProperty("telegram.bot.username", "").trim()
val telegramAuthOrigin: String = localProperties.getProperty("telegram.auth.origin", "https://teleport.app").trim()
fun resolveApiBaseUrl(): String {
    localProperties.getProperty("api.base.url", "").trim().takeIf { it.isNotEmpty() }?.let { return it }
    val envFile = rootProject.file("../public_url.env")
    if (envFile.exists()) {
        for (line in envFile.readLines()) {
            if (line.startsWith("PUBLIC_URL=")) {
                val u = line.substringAfter("=").trim().trimEnd('/') + "/"
                if (u.startsWith("http")) return u
            }
        }
    }
    return "https://api.example.com/"
}
val apiBaseUrl: String = resolveApiBaseUrl()
val googleServicesFile = file("google-services.json")
val firebaseSmsEnabled = googleServicesFile.exists()

if (firebaseSmsEnabled) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.teleport.messenger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.teleport.messenger"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "YANDEX_CLIENT_ID", "\"$yandexClientId\"")
        buildConfigField("String", "TELEGRAM_BOT_TOKEN", "\"${telegramBotToken.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        buildConfigField("String", "TELEGRAM_BOT_USERNAME", "\"$telegramBotUsername\"")
        buildConfigField("String", "TELEGRAM_AUTH_ORIGIN", "\"$telegramAuthOrigin\"")
        buildConfigField("boolean", "FIREBASE_SMS", firebaseSmsEnabled.toString())
        manifestPlaceholders["YANDEX_CLIENT_ID"] = yandexClientId
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("com.yandex.android:authsdk:3.1.3")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.window:window:1.2.0")

    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("com.google.zxing:core:3.5.3")
    implementation("androidx.browser:browser:1.8.0")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("io.getstream:stream-webrtc-android:1.1.3")
}
