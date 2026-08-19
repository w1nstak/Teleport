import Foundation

struct Message: Identifiable, Codable {
    let id: String
    let chatId: String
    let senderId: String
    var text: String?
    var imageURL: String?
    var voiceURL: String?
    var fileURL: String?
    var fileName: String?
    var replyToId: String?
    var isRead: Bool
    var isEdited: Bool
    let createdAt: Date

    enum MessageType: String, Codable {
        case text, image, voice, file
    }
    var type: MessageType
}
