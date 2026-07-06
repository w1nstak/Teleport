package com.teleport.messenger.auth

/** Как отправляется и проверяется код подтверждения. */
enum class SmsAuthMode {
    /** Google Firebase Phone Auth — СМС с серверов Google */
    FIREBASE,
    /** Сервер Teleport + sms.ru / smsc.ru */
    SERVER,
    /** Локально: SIM телефона или код на экране */
    LOCAL,
}
