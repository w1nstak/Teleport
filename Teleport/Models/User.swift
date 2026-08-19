import Foundation

struct TeleportUser: Identifiable, Codable {
    let id: String
    var phone: String
    var displayName: String
    var avatarURL: String?
    var bio: String?
    var lastSeen: Date?
    var isOnline: Bool
}
