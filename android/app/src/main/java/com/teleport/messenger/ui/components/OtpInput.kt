package com.teleport.messenger.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    length: Int = 6,
    modifier: Modifier = Modifier,
    boxColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(length) { index ->
                val char = code.getOrNull(index)?.toString() ?: ""
                Surface(
                    modifier = Modifier.weight(1f).aspectRatio(0.85f),
                    shape = RoundedCornerShape(14.dp),
                    color = boxColor,
                    tonalElevation = 0.dp,
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            char,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = code,
            onValueChange = { v ->
                if (v.length <= length && v.all { it.isDigit() }) onCodeChange(v)
            },
            modifier = Modifier
                .matchParentSize()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedTextColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedTextColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
        )
    }
}
