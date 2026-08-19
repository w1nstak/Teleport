import Foundation
import SwiftUI

class ChatService: ObservableObject {
    @Published var chats: [Chat] = []
    @Published var isLoading = false

    func loadChats() {
        isLoading = true
        // TODO: Replace with real backend fetch
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.chats = Self.mockChats
            self.isLoading = false
        }
    }

    func createChat(with userId: String, title: String) -> Chat {
        let chat = Chat(
            id: UUID().uuidString,
            title: title,
            isGroup: false,
            participants: [userId],
            lastMessage: nil,
            unreadCount: 0,
            isPinned: false,
            isMuted: false,
            updatedAt: Date()
        )
        chats.insert(chat, at: 0)
        return chat
    }

    static let mockChats: [Chat] = [
        Chat(id: "1", title: "Алексей", isGroup: false, participants: ["u2"],
             lastMessage: Message(id: "m1", chatId: "1", senderId: "u2", text: "Привет! Как дела?", isRead: true, isEdited: false, createdAt: Date(), type: .text),
             unreadCount: 0, isPinned: true, isMuted: false, updatedAt: Date()),
        Chat(id: "2", title: "Работа", isGroup: true, participants: ["u2", "u3", "u4"],
             lastMessage: Message(id: "m2", chatId: "2", senderId: "u3", text: "Завтра митинг в 10:00", isRead: false, isEdited: false, createdAt: Date().addingTimeInterval(-3600), type: .text),
             unreadCount: 3, isPinned: false, isMuted: false, updatedAt: Date().addingTimeInterval(-3600)),
        Chat(id: "3", title: "Мария", isGroup: false, participants: ["u5"],
             lastMessage: Message(id: "m3", chatId: "3", senderId: "u5", text: "Скинь фотку", isRead: true, isEdited: false, createdAt: Date().addingTimeInterval(-7200), type: .text),
             unreadCount: 0, isPinned: false, isMuted: true, updatedAt: Date().addingTimeInterval(-7200)),
    ]
}
