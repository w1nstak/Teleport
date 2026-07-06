package com.teleport.messenger.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleport.messenger.ui.strings.*
import com.teleport.messenger.ui.theme.TeleportAppTheme
import com.teleport.messenger.viewmodel.TeleportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalizationScreen(vm: TeleportViewModel, onBack: () -> Unit) {
    val settings by vm.settings().collectAsState(initial = null)
    val colors = TeleportAppTheme.colors
    var overrides by remember(settings) {
        mutableStateOf(parseLocaleOverrides(settings?.localeOverridesJson))
    }
    var editingKey by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }

    fun persist(newOverrides: Map<String, String>) {
        overrides = newOverrides
        settings?.let {
            vm.updateSettings(it.copy(localeOverridesJson = encodeLocaleOverrides(newOverrides)))
        }
    }

    val grouped = remember(overrides) {
        LocalizableStringEntries.groupBy { it.section }
    }

    Scaffold(
        containerColor = colors.screenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        appStr(AppStringKey.LOCALIZATION_TITLE),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, appStr(AppStringKey.CANCEL), tint = colors.textMuted)
                    }
                },
                actions = {
                    TextButton(onClick = { persist(emptyMap()) }) {
                        Text(appStr(AppStringKey.RESET), color = colors.accentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBg,
                    titleContentColor = colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            grouped.forEach { (sectionKey, entries) ->
                item {
                    Text(
                        appStr(sectionKey),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = colors.textMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.cardBg,
                    ) {
                        Column {
                            entries.forEachIndexed { index, entry ->
                                val current = overrides[entry.key] ?: DefaultAppStrings[entry.key] ?: entry.key
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            editingKey = entry.key
                                            draft = overrides[entry.key] ?: DefaultAppStrings[entry.key] ?: ""
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        DefaultAppStrings[entry.key] ?: entry.key,
                                        color = colors.textMuted,
                                        fontSize = 15.sp,
                                    )
                                    Text(
                                        current,
                                        color = colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                if (index < entries.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 16.dp),
                                        color = colors.divider,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    editingKey?.let { key ->
        AlertDialog(
            onDismissRequest = { editingKey = null },
            title = { Text(appStr(AppStringKey.EDIT_LABEL)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${appStr(AppStringKey.ORIGINAL_LABEL)}: ${DefaultAppStrings[key] ?: key}",
                        color = colors.textMuted,
                        fontSize = 14.sp,
                    )
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        label = { Text(appStr(AppStringKey.CUSTOM_LABEL)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val next = overrides.toMutableMap()
                    val trimmed = draft.trim()
                    if (trimmed.isEmpty() || trimmed == DefaultAppStrings[key]) {
                        next.remove(key)
                    } else {
                        next[key] = trimmed
                    }
                    persist(next)
                    editingKey = null
                }) {
                    Text(appStr(AppStringKey.SAVE), color = colors.accentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingKey = null }) {
                    Text(appStr(AppStringKey.CANCEL), color = colors.accentRed)
                }
            },
            containerColor = colors.cardBg,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textPrimary,
        )
    }
}
