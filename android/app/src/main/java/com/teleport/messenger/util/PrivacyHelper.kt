package com.teleport.messenger.util

import com.teleport.messenger.data.entity.UserEntity

object PrivacyLevel {
    const val EVERYONE = "everyone"
    const val CONTACTS = "contacts"
    const val NOBODY = "nobody"
}

object PrivacyHelper {

    fun displayName(user: UserEntity, viewerIsSelf: Boolean, isContact: Boolean): String {
        if (viewerIsSelf) return user.displayName
        if (user.anonymousMode && !user.anonymousAlias.isNullOrBlank()) {
            return user.anonymousAlias!!
        }
        if (user.anonymousMode) return "Анонимный пользователь"
        return user.displayName
    }

    fun canSeePhone(user: UserEntity, viewerIsSelf: Boolean, isContact: Boolean): Boolean {
        if (viewerIsSelf) return true
        if (user.anonymousMode) return false
        return allows(user.privacyPhone, isContact)
    }

    fun canSeeLastSeen(user: UserEntity, viewerIsSelf: Boolean, isContact: Boolean): Boolean {
        if (viewerIsSelf) return true
        if (user.anonymousMode) return false
        return allows(user.privacyLastSeen, isContact)
    }

    fun canSeePhoto(user: UserEntity, viewerIsSelf: Boolean, isContact: Boolean): Boolean {
        if (viewerIsSelf) return true
        if (user.anonymousMode) return false
        return allows(user.privacyPhoto, isContact)
    }

    fun onlineStatus(user: UserEntity, viewerIsSelf: Boolean, isContact: Boolean, hideOwnOnline: Boolean): String {
        if (viewerIsSelf && hideOwnOnline) return "offline"
        if (!canSeeLastSeen(user, viewerIsSelf, isContact)) return "offline"
        return when {
            user.status.isNotBlank() -> user.status
            user.isOnline -> "online"
            else -> "offline"
        }
    }

    fun levelLabel(level: String): String = when (level) {
        PrivacyLevel.EVERYONE -> "Все"
        PrivacyLevel.CONTACTS -> "Контакты"
        PrivacyLevel.NOBODY -> "Никто"
        else -> level
    }

    private fun allows(level: String, isContact: Boolean): Boolean = when (level) {
        PrivacyLevel.EVERYONE -> true
        PrivacyLevel.CONTACTS -> isContact
        PrivacyLevel.NOBODY -> false
        else -> true
    }
}
