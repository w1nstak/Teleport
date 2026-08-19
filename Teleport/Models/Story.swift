import Foundation

struct Story: Identifiable, Codable {
    let id: String
    let userId: String
    var mediaURL: String
    var mediaType: MediaType
    var caption: String?
    var viewerIds: [String]
    var reactions: [Reaction]
    var expiresAt: Date
    let createdAt: Date

    enum MediaType: String, Codable {
        case photo, video
    }
}

struct StoryCircle: Identifiable {
    let id: String
    var userName: String
    var avatarURL: String?
    var hasUnseenStory: Bool
    var stories: [Story]
}
