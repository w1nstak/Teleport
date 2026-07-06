package com.teleport.messenger.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit = {}) {
        val manager = BiometricManager.from(activity)
        if (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            != BiometricManager.BIOMETRIC_SUCCESS
        ) {
            onError("Biometric not available")
            return
        }
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onError(errString.toString())
            })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
            .setTitle("Teleport")
            .setSubtitle("Подтвердите личность")
            .setNegativeButtonText("PIN")
            .build())
    }
}
