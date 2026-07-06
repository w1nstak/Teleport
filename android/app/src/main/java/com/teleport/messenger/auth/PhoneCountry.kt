package com.teleport.messenger.auth

import com.teleport.messenger.R

data class PhoneCountry(
    val code: String,
    val dialCode: String,
    val flagRes: Int,
    val name: String,
    val digits: Int,
    val placeholder: String,
)

val SupportedCountries = listOf(
    PhoneCountry("RU", "+7", R.drawable.ic_flag_ru, "Россия", 10, "123 456-78-90"),
    PhoneCountry("BY", "+375", R.drawable.ic_flag_by, "Беларусь", 9, "29 123-45-67"),
)

fun PhoneCountry.formatDigits(raw: String): String {
    val d = raw.filter { it.isDigit() }.take(digits)
    return when (dialCode) {
        "+7" -> when {
            d.length <= 3 -> d
            d.length <= 6 -> "${d.take(3)} ${d.drop(3)}"
            d.length <= 8 -> "${d.take(3)} ${d.substring(3, 6)}-${d.drop(6)}"
            else -> "${d.take(3)} ${d.substring(3, 6)}-${d.substring(6, 8)}-${d.drop(8)}"
        }
        "+375" -> when {
            d.length <= 2 -> d
            d.length <= 5 -> "${d.take(2)} ${d.drop(2)}"
            d.length <= 7 -> "${d.take(2)} ${d.substring(2, 5)}-${d.drop(5)}"
            else -> "${d.take(2)} ${d.substring(2, 5)}-${d.substring(5, 7)}-${d.drop(7)}"
        }
        else -> d
    }
}

fun PhoneCountry.isValid(raw: String): Boolean =
    raw.filter { it.isDigit() }.length == digits

fun PhoneCountry.fullNumber(raw: String): String =
    dialCode + raw.filter { it.isDigit() }
