import Foundation
import SwiftUI

@MainActor
final class AppViewModel: ObservableObject {
    @Published var token: String?
    @Published var user: UserDto?
    @Published var chats: [ChatListItem] = []
    @Published var messages: [MessageDto] = []
    @Published var currentChatId: String?
    @Published var currentChatTitle: String = ""
    @Published var currentPeer: UserDto?
    @Published var searchResults: [UserDto] = []
    @Published var errorMessage: String?
    @Published var isLoading = false
    @Published var settings = AppSettings()
    @Published var recentCalls: [CallRecord] = []
    @Published var isOwner = false
    @Published var adminStats: AdminStatsDto?

    private let api = TeleportAPI.shared
    private let ws = WebSocketClient()
    private let tokenKey = "teleport_ios_token"
    private let settingsKey = "teleport_ios_settings"

    init() {
        loadSettings()
        token = UserDefaults.standard.string(forKey: tokenKey)
        ws.onMessage = { [weak self] msg in
            Task { @MainActor in self?.handleIncoming(msg) }
        }
    }

    func restoreSession() async {
        guard let token else { return }
        do {
            try await afterLogin(token: token)
        } catch {
            logout()
        }
    }

    func login(username: String, password: String) async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let res = try await api.login(username: username.removePrefix("@").trimmingCharacters(in: .whitespaces), password: password)
            try await afterLogin(token: res.token)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func register(displayName: String, username: String, password: String) async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let res = try await api.register(
                displayName: displayName.trimmingCharacters(in: .whitespaces),
                username: username.removePrefix("@").trimmingCharacters(in: .whitespaces),
                password: password
            )
            try await afterLogin(token: res.token)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func sendSms(phone: String) async -> String? {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let res = try await api.sendSms(phone: phone)
            return res.devCode
        } catch {
            errorMessage = error.localizedDescription
            return nil
        }
    }

    private func afterLogin(token: String) async throws {
        self.token = token
        UserDefaults.standard.set(token, forKey: tokenKey)
        user = try await api.me(token: token)
        await refreshOwnerStatus()
        try await loadChats()
        ws.connect(token: token)
    }

    func refreshOwnerStatus() async {
        guard let token else {
            isOwner = false
            return
        }
        do {
            let check = try await api.adminCheck(token: token)
            isOwner = check.isOwner
        } catch {
            isOwner = false
        }
    }

    func loadAdminStats() async {
        guard let token, isOwner else { return }
        isLoading = true
        defer { isLoading = false }
        do {
            adminStats = try await api.adminStats(token: token)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func logout() {
        ws.disconnect()
        token = nil
        user = nil
        isOwner = false
        adminStats = nil
        chats = []
        messages = []
        currentChatId = nil
        UserDefaults.standard.removeObject(forKey: tokenKey)
    }

    func loadChats() async throws {
        guard let token else { return }
        chats = try await api.listChats(token: token)
    }

    func reloadChats() async {
        do { try await loadChats() } catch { errorMessage = error.localizedDescription }
    }

    func openChat(_ chat: ChatListItem) async {
        currentChatId = chat.chatId
        currentChatTitle = chat.title
        currentPeer = nil
        await loadMessages()
    }

    func openChatWithUser(_ user: UserDto) async {
        guard let token else { return }
        isLoading = true
        defer { isLoading = false }
        do {
            let res = try await api.openChat(token: token, otherUserId: user.id)
            try await loadChats()
            currentChatId = res.chatId
            currentChatTitle = res.peer?.displayName ?? res.title
            currentPeer = res.peer
            await loadMessages()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func loadMessages() async {
        guard let token, let chatId = currentChatId else { return }
        do {
            messages = try await api.messages(token: token, chatId: chatId)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func sendMessage(_ text: String) async {
        guard let token, let chatId = currentChatId, !text.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        do {
            let msg = try await api.sendMessage(token: token, chatId: chatId, text: text.trimmingCharacters(in: .whitespaces))
            appendMessage(msg)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func searchUsers(_ query: String) async {
        guard let token, query.count >= 1 else {
            searchResults = []
            return
        }
        do {
            searchResults = try await api.searchUsers(token: token, q: query)
        } catch {
            searchResults = []
        }
    }

    func updateProfile(displayName: String?, username: String?, bio: String?) async {
        guard let token else { return }
        isLoading = true
        defer { isLoading = false }
        do {
            user = try await api.updateMe(token: token, body: UpdateProfileRequest(displayName: displayName, username: username, bio: bio))
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    var displayChats: [ChatListItem] {
        chats.filter { $0.type != "SAVED" && $0.type != "ARCHIVED" }
    }

    func favoritesChat() -> ChatListItem? {
        chats.first { $0.type == "SAVED" }
    }

    func markAllRead() {
        // Local UX — server has no bulk endpoint yet
    }
        // Local UX — server has no bulk endpoint yet
    }

    func saveSettings() {
        if let data = try? JSONEncoder().encode(settings) {
            UserDefaults.standard.set(data, forKey: settingsKey)
        }
    }

    private func loadSettings() {
        guard let data = UserDefaults.standard.data(forKey: settingsKey),
              let s = try? JSONDecoder().decode(AppSettings.self, from: data) else { return }
        settings = s
    }

    private func handleIncoming(_ msg: MessageDto) {
        if msg.chatId == currentChatId {
            appendMessage(msg)
        }
        if !chats.contains(where: { $0.chatId == msg.chatId }) {
            Task { await reloadChats() }
        }
    }

    private func appendMessage(_ msg: MessageDto) {
        guard !messages.contains(where: { $0.id == msg.id }) else { return }
        messages.append(msg)
    }

    func label(_ key: String) -> String {
        str(key, overrides: settings.localeOverrides)
    }
}

struct CallRecord: Identifiable, Hashable {
    let id: String
    let title: String
    let type: String
    let time: Date
}

private extension String {
    func removePrefix(_ prefix: String) -> String {
        hasPrefix(prefix) ? String(dropFirst(prefix.count)) : self
    }
}
