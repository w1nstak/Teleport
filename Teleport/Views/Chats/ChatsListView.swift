import SwiftUI

struct ChatsListView: View {
    @StateObject private var chatService = ChatService()
    @State private var searchText = ""

    var filteredChats: [Chat] {
        if searchText.isEmpty { return chatService.chats }
        return chatService.chats.filter { $0.title.localizedCaseInsensitiveContains(searchText) }
    }

    private let mockStories: [StoryCircle] = [
        StoryCircle(id: "s1", userName: "Алексей", hasUnseenStory: true, stories: []),
        StoryCircle(id: "s2", userName: "Мария", hasUnseenStory: true, stories: []),
        StoryCircle(id: "s3", userName: "Работа", hasUnseenStory: false, stories: []),
    ]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ChatFoldersTabView()
                StoriesRowView(stories: mockStories)
                Divider()

                List {
                    ForEach(filteredChats) { chat in
                        NavigationLink(destination: ChatView(chat: chat)) {
                            ChatRowView(chat: chat)
                        }
                    }
                    .onDelete { _ in }
                }
                .listStyle(.plain)
            }
            .searchable(text: $searchText, prompt: "Поиск")
            .navigationTitle("Teleport")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {}) {
                        Image(systemName: "square.and.pencil")
                    }
                }
            }
            .onAppear { chatService.loadChats() }
        }
    }
}

struct ChatRowView: View {
    let chat: Chat

    var body: some View {
        HStack(spacing: 12) {
            Circle()
                .fill(TeleportTheme.primaryColor.opacity(0.2))
                .frame(width: 52, height: 52)
                .overlay(
                    Text(String(chat.title.prefix(1)))
                        .font(.title2.bold())
                        .foregroundColor(TeleportTheme.primaryColor)
                )

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(chat.title)
                        .font(.body.bold())
                        .foregroundColor(TeleportTheme.textPrimary)
                    Spacer()
                    if let msg = chat.lastMessage {
                        Text(timeString(msg.createdAt))
                            .font(.caption)
                            .foregroundColor(TeleportTheme.textSecondary)
                    }
                }

                HStack {
                    Text(chat.lastMessage?.text ?? "")
                        .font(.subheadline)
                        .foregroundColor(TeleportTheme.textSecondary)
                        .lineLimit(1)
                    Spacer()
                    if chat.unreadCount > 0 {
                        Text("\(chat.unreadCount)")
                            .font(.caption2.bold())
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(TeleportTheme.primaryColor)
                            .clipShape(Capsule())
                    }
                }
            }
        }
        .padding(.vertical, 4)
    }

    private func timeString(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
}
