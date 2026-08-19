import Foundation

struct Chat: Identifiable, Codable {
    let id: String
    var title: String
    var isGroup: Bool
    var avatarURL: String?
    var participants: [String]
    var lastMessage: Message?
    var unreadCount: Int
    var isPinned: Bool
    var isMuted: Bool
    var updatedAt: Date
}
