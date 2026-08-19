import Foundation

struct Channel: Identifiable, Codable {
    let id: String
    var title: String
    var description: String?
    var avatarURL: String?
    var ownerId: String
    var adminIds: [String]
    var subscriberCount: Int
    var isPublic: Bool
    var username: String?
    var lastPost: ChannelPost?
    var isMuted: Bool
    var createdAt: Date
}

struct ChannelPost: Identifiable, Codable {
    let id: String
    let channelId: String
    var text: String?
    var imageURLs: [String]?
    var fileURL: String?
    var viewCount: Int
    var reactions: [Reaction]
    var isPinned: Bool
    let createdAt: Date
}

struct Reaction: Codable {
    var emoji: String
    var count: Int
    var userIds: [String]
}
