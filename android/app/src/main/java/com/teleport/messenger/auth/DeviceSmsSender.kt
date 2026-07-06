package com.teleport.messenger.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

enum class SmsDelivery {
    /** СМС отправлено через SIM-карту телефона */
    SENT,
    /** Нет SIM / нет разрешения — код показываем на экране */
    ON_SCREEN,
}

/** Отправка СМС с самого телефона (SIM-карта), без сторонних сервисов. */
class DeviceSmsSender(private val context: Context) {

    fun hasSendPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED

    fun hasSimReady(): Boolean {
        val tm = context.getSystemService(TelephonyManager::class.java) ?: return false
        return tm.simState == TelephonyManager.SIM_STATE_READY
    }

    fun canSendViaSim(): Boolean = hasSimReady() && hasSendPermission()

    fun send(phone: String, code: String, permissionGranted: Boolean): Result<SmsDelivery> {
        if (!permissionGranted || !hasSimReady()) {
            return Result.success(SmsDelivery.ON_SCREEN)
        }
        return try {
            val message = "$code — код для входа в Teleport.\nНе сообщайте никому."
            val manager = if (Build.VERSION.SDK_INT >= 31) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            } ?: return Result.failure(IllegalStateException("SmsManager недоступен"))
            manager.sendTextMessage(phone, null, message, null, null)
            Result.success(SmsDelivery.SENT)
        } catch (e: SecurityException) {
            Result.success(SmsDelivery.ON_SCREEN)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
