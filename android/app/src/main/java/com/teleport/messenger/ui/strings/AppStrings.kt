package com.teleport.messenger.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import org.json.JSONObject

object AppStringKey {
    const val CHATS_TITLE = "chats_title"
    const val SEARCH = "search"
    const val ARCHIVE = "archive"
    const val READ_ALL = "read_all"
    const val SELECT_CHATS = "select_chats"
    const val DONE = "done"
    const val NAV_CHATS = "nav_chats"
    const val NAV_PROFILE = "nav_profile"
    const val NAV_SETTINGS = "nav_settings"
    const val NAV_SEARCH = "nav_search"
    const val MESSAGE_PLACEHOLDER = "message_placeholder"
    const val NEW_CHAT = "new_chat"
    const val LOCALIZATION = "localization"
    const val LOCALIZATION_TITLE = "localization_title"
    const val LOCALIZATION_SECTION_CHATS = "localization_section_chats"
    const val LOCALIZATION_SECTION_NAV = "localization_section_nav"
    const val EDIT_LABEL = "edit_label"
    const val ORIGINAL_LABEL = "original_label"
    const val CUSTOM_LABEL = "custom_label"
    const val RESET = "reset"
    const val SAVE = "save"
    const val CANCEL = "cancel"
    const val ALL_CHATS = "all_chats"
    const val NO_MESSAGES = "no_messages"
    const val SETTINGS = "settings"
    const val APPEARANCE = "appearance"
}

val DefaultAppStrings: Map<String, String> = mapOf(
    AppStringKey.CHATS_TITLE to "Чаты",
    AppStringKey.SEARCH to "Поиск",
    AppStringKey.ARCHIVE to "Архив",
    AppStringKey.READ_ALL to "Прочитать все",
    AppStringKey.SELECT_CHATS to "Выбрать чаты",
    AppStringKey.DONE to "Готово",
    AppStringKey.NAV_CHATS to "Чаты",
    AppStringKey.NAV_PROFILE to "Вы",
    AppStringKey.NAV_SETTINGS to "Настройки",
    AppStringKey.NAV_SEARCH to "Поиск",
    AppStringKey.MESSAGE_PLACEHOLDER to "Сообщение...",
    AppStringKey.NEW_CHAT to "Новый чат",
    AppStringKey.LOCALIZATION to "Локализация",
    AppStringKey.LOCALIZATION_TITLE to "Локализация",
    AppStringKey.LOCALIZATION_SECTION_CHATS to "Чаты",
    AppStringKey.LOCALIZATION_SECTION_NAV to "Навигация",
    AppStringKey.EDIT_LABEL to "Изменить надпись",
    AppStringKey.ORIGINAL_LABEL to "Оригинал",
    AppStringKey.CUSTOM_LABEL to "Ваша надпись",
    AppStringKey.RESET to "Сбросить",
    AppStringKey.SAVE to "Сохранить",
    AppStringKey.CANCEL to "Отмена",
    AppStringKey.ALL_CHATS to "Все",
    AppStringKey.NO_MESSAGES to "Нет сообщений",
    AppStringKey.SETTINGS to "Настройки",
    AppStringKey.APPEARANCE to "Оформление",
)

data class AppStringEntry(val key: String, val section: String)

val LocalizableStringEntries = listOf(
    AppStringEntry(AppStringKey.CHATS_TITLE, AppStringKey.LOCALIZATION_SECTION_CHATS),
    AppStringEntry(AppStringKey.SEARCH, AppStringKey.LOCALIZATION_SECTION_CHATS),
    AppStringEntry(AppStringKey.ARCHIVE, AppStringKey.LOCALIZATION_SECTION_CHATS),
    AppStringEntry(AppStringKey.READ_ALL, AppStringKey.LOCALIZATION_SECTION_CHATS),
    AppStringEntry(AppStringKey.SELECT_CHATS, AppStringKey.LOCALIZATION_SECTION_CHATS),
    AppStringEntry(AppStringKey.DONE, AppStringKey.LOCALIZATION_SECTION_CHATS),
    AppStringEntry(AppStringKey.NAV_CHATS, AppStringKey.LOCALIZATION_SECTION_NAV),
    AppStringEntry(AppStringKey.NAV_PROFILE, AppStringKey.LOCALIZATION_SECTION_NAV),
    AppStringEntry(AppStringKey.NAV_SETTINGS, AppStringKey.LOCALIZATION_SECTION_NAV),
    AppStringEntry(AppStringKey.NAV_SEARCH, AppStringKey.LOCALIZATION_SECTION_NAV),
    AppStringEntry(AppStringKey.MESSAGE_PLACEHOLDER, AppStringKey.LOCALIZATION_SECTION_CHATS),
    AppStringEntry(AppStringKey.NEW_CHAT, AppStringKey.LOCALIZATION_SECTION_CHATS),
)

fun parseLocaleOverrides(json: String?): Map<String, String> {
    if (json.isNullOrBlank() || json == "{}") return emptyMap()
    return runCatching {
        val obj = JSONObject(json)
        buildMap {
            obj.keys().forEach { key ->
                val value = obj.optString(key, "").trim()
                if (value.isNotEmpty()) put(key, value)
            }
        }
    }.getOrDefault(emptyMap())
}

fun mergeAppStrings(overridesJson: String?): Map<String, String> {
    val overrides = parseLocaleOverrides(overridesJson)
    return DefaultAppStrings.mapValues { (key, default) -> overrides[key] ?: default }
}

fun encodeLocaleOverrides(overrides: Map<String, String>): String {
    val obj = JSONObject()
    overrides.forEach { (key, value) ->
        if (value.isNotBlank() && value != DefaultAppStrings[key]) obj.put(key, value)
    }
    return obj.toString()
}

val LocalAppStrings = staticCompositionLocalOf { DefaultAppStrings }

@Composable
fun appStr(key: String): String = LocalAppStrings.current[key] ?: DefaultAppStrings[key] ?: key
