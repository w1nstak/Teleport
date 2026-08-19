import Foundation

struct Group: Identifiable, Codable {
    let id: String
    var title: String
    var description: String?
    var avatarURL: String?
    var ownerId: String
    var adminIds: [String]
    var memberIds: [String]
    var memberCount: Int
    var isPublic: Bool
    var username: String?
    var permissions: GroupPermissions
    var createdAt: Date
}

struct GroupPermissions: Codable {
    var canSendMessages: Bool
    var canSendMedia: Bool
    var canAddMembers: Bool
    var canPinMessages: Bool
    var canEditInfo: Bool
    var slowModeInterval: Int?
}
