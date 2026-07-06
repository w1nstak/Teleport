package com.teleport.messenger.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.teleport.messenger.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/** Отправка и проверка СМС через Firebase Phone Auth (Google). */
class PhoneAuthHandler(private val activity: FragmentActivity) {
    val isAvailable: Boolean = BuildConfig.FIREBASE_SMS

    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var autoCredential: PhoneAuthCredential? = null

    suspend fun sendVerificationCode(phone: String, forceResend: Boolean = false): Result<Unit> {
        if (!isAvailable) {
            return Result.failure(
                IllegalStateException(
                    "Firebase не настроен. Добавьте google-services.json — см. FIREBASE_SMS_SETUP.md",
                ),
            )
        }
        autoCredential = null
        return suspendCancellableCoroutine { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    autoCredential = credential
                    if (cont.isActive) cont.resume(Result.success(Unit))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    if (cont.isActive) cont.resume(Result.failure(Exception(mapFirebaseError(e))))
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    verificationId = id
                    resendToken = token
                    if (cont.isActive) cont.resume(Result.success(Unit))
                }
            }
            val builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
            if (forceResend && resendToken != null) {
                builder.setForceResendingToken(resendToken!!)
            }
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }
    }

    fun isAutoVerified(): Boolean = autoCredential != null

    suspend fun verifyCode(code: String): Result<Unit> {
        val credential = autoCredential ?: run {
            val id = verificationId
                ?: return Result.failure(IllegalStateException("Сначала запросите код"))
            PhoneAuthProvider.getCredential(id, code.trim())
        }
        return suspendCancellableCoroutine { cont ->
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    auth.signOut()
                    autoCredential = null
                    verificationId = null
                    if (cont.isActive) cont.resume(Result.success(Unit))
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(Result.failure(it))
                }
        }
    }

    private fun mapFirebaseError(e: FirebaseException): String {
        val msg = e.localizedMessage ?: e.message.orEmpty()
        return when {
            msg.contains("quota", true) -> "Превышен лимит SMS. Попробуйте позже"
            msg.contains("blocked", true) -> "Отправка SMS заблокирована. Проверьте Firebase Console"
            msg.contains("invalid", true) -> "Неверный номер телефона"
            msg.contains("network", true) -> "Нет связи. Проверьте интернет"
            msg.isNotBlank() -> msg
            else -> "Не удалось отправить СМС"
        }
    }
}

@Composable
fun rememberPhoneAuthHandler(): PhoneAuthHandler {
    val activity = LocalContext.current as FragmentActivity
    return remember(activity) { PhoneAuthHandler(activity) }
}
