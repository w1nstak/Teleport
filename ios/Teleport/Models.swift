import Foundation

struct AuthResponse: Codable {
    let token: String
    let userId: String
    let accountId: String
    let phone: String?
    let displayName: String?
    let username: String?
}

struct UsernameLoginRequest: Codable {
    let username: String
    let password: String
}

struct WebRegisterRequest: Codable {
    let displayName: String
    let username: String
    let password: String
}

struct UserDto: Codable, Identifiable, Hashable {
    let id: String
    let displayName: String
    let username: String?
    let bio: String?
    let isOnline: Bool?
    let lastSeen: Int64?
    let isPremium: Bool?
}

struct ChatListItem: Codable, Identifiable, Hashable {
    var id: String { chatId }
    let chatId: String
    let title: String
    let type: String
    let members: [String]?
}

struct OpenChatRequest: Codable {
    let otherUserId: String
}

struct OpenChatResponse: Codable {
    let chatId: String
    let title: String
    let type: String
    let members: [String]?
    let peer: UserDto?
}

struct MessageDto: Codable, Identifiable, Hashable {
    let id: String
    let chatId: String
    let senderId: String
    let type: String
    let text: String
    let mediaUri: String?
    let replyToId: String?
    let forwardFromId: String?
    let isEdited: Bool?
    let editedAt: Int64?
    let createdAt: Int64
}

struct MessagesPage: Codable {
    let messages: [MessageDto]
}

struct SendMessageRequest: Codable {
    let chatId: String
    let type: String
    let text: String
}

struct UpdateProfileRequest: Codable {
    var displayName: String?
    var username: String?
    var bio: String?
}

struct SmsSendRequest: Codable { let phone: String }
struct SmsSendResponse: Codable { let ok: Bool; let devCode: String?; let retryAfter: Int? }
struct SmsVerifyRequest: Codable { let phone: String; let code: String }
struct SmsVerifyResponse: Codable { let ok: Bool }

struct WsEnvelope: Codable {
    let event: String
    let payload: MessageDto?
}

struct AppSettings: Codable {
    var notificationsEnabled: Bool = true
    var powerSavingEnabled: Bool = false
    var themeMode: String = "dark"
    var localeOverrides: [String: String] = [:]
}

enum MainTab: String, CaseIterable {
    case chats, contacts, profile, settings, calls

    var title: String {
        switch self {
        case .chats: return "Чаты"
        case .contacts: return "Контакты"
        case .profile: return "Вы"
        case .settings: return "Настройки"
        case .calls: return "Звонки"
        }
    }

    var icon: String {
        switch self {
        case .chats: return "bubble.left.and.bubble.right"
        case .contacts: return "person.2"
        case .profile: return "person.crop.circle"
        case .settings: return "gearshape"
        case .calls: return "phone"
        }
    }
}

func str(_ key: String, overrides: [String: String]) -> String {
    let defaults: [String: String] = [
        "chats_title": "Чаты",
        "nav_chats": "Чаты",
        "nav_contacts": "Контакты",
        "nav_profile": "Вы",
        "nav_settings": "Настройки",
        "nav_calls": "Звонки",
        "search": "Поиск",
        "message_placeholder": "Сообщение...",
    ]
    return overrides[key] ?? defaults[key] ?? key
}

func initials(_ name: String?) -> String {
    guard let name, !name.isEmpty else { return "?" }
    return name.split(separator: " ")
        .prefix(2)
        .compactMap { $0.first }
        .map { String($0).uppercased() }
        .joined()
}

func formatTime(_ ts: Int64) -> String {
    let date = Date(timeIntervalSince1970: TimeInterval(ts) / 1000)
    let f = DateFormatter()
    f.locale = Locale(identifier: "ru_RU")
    f.dateFormat = "HH:mm"
    return f.string(from: date)
}
