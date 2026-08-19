import Foundation
import SwiftUI

class MessageService: ObservableObject {
    @Published var messages: [Message] = []
    @Published var isLoading = false

    func loadMessages(chatId: String) {
        isLoading = true
        // TODO: Replace with real backend fetch + realtime subscription
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            self.messages = Self.mockMessages(chatId: chatId)
            self.isLoading = false
        }
    }

    func sendMessage(chatId: String, senderId: String, text: String) {
        let message = Message(
            id: UUID().uuidString,
            chatId: chatId,
            senderId: senderId,
            text: text,
            isRead: false,
            isEdited: false,
            createdAt: Date(),
            type: .text
        )
        messages.append(message)
        // TODO: Send to backend
    }

    static func mockMessages(chatId: String) -> [Message] {
        let now = Date()
        return [
            Message(id: "m10", chatId: chatId, senderId: "u2", text: "Привет!", isRead: true, isEdited: false, createdAt: now.addingTimeInterval(-600), type: .text),
            Message(id: "m11", chatId: chatId, senderId: "me", text: "Привет! Как дела?", isRead: true, isEdited: false, createdAt: now.addingTimeInterval(-540), type: .text),
            Message(id: "m12", chatId: chatId, senderId: "u2", text: "Всё отлично, работаю над проектом", isRead: true, isEdited: false, createdAt: now.addingTimeInterval(-480), type: .text),
            Message(id: "m13", chatId: chatId, senderId: "me", text: "Круто! Скинь потом результат", isRead: true, isEdited: false, createdAt: now.addingTimeInterval(-420), type: .text),
            Message(id: "m14", chatId: chatId, senderId: "u2", text: "Договорились 👍", isRead: true, isEdited: false, createdAt: now.addingTimeInterval(-360), type: .text),
        ]
    }
}
