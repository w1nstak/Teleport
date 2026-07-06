package com.teleport.messenger.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.teleport.messenger.auth.PhoneCountry
import com.teleport.messenger.auth.formatDigits

class PhoneVisualTransformation(private val country: PhoneCountry) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(country.digits)
        val formatted = country.formatDigits(digits)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatted.length
            override fun transformedToOriginal(offset: Int): Int = digits.length
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
