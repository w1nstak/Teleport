import SwiftUI

struct ForwardView: View {
    let message: Message
    @Binding var isPresented: Bool
    @StateObject private var chatService = ChatService()
    @State private var searchText = ""

    var filteredChats: [Chat] {
        if searchText.isEmpty { return chatService.chats }
        return chatService.chats.filter { $0.title.localizedCaseInsensitiveContains(searchText) }
    }

    var body: some View {
        NavigationStack {
            List {
                ForEach(filteredChats) { chat in
                    Button(action: {
                        // TODO: forward message to chat
                        isPresented = false
                    }) {
                        HStack(spacing: 12) {
                            Circle()
                                .fill(TeleportTheme.primaryColor.opacity(0.2))
                                .frame(width: 40, height: 40)
                                .overlay(
                                    Text(String(chat.title.prefix(1)))
                                        .foregroundColor(TeleportTheme.primaryColor)
                                )
                            Text(chat.title)
                        }
                    }
                }
            }
            .listStyle(.plain)
            .searchable(text: $searchText, prompt: "Кому переслать...")
            .navigationTitle("Переслать")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Отмена") { isPresented = false }
                }
            }
            .onAppear { chatService.loadChats() }
        }
    }
}
