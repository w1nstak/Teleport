import SwiftUI

struct GlobalSearchView: View {
    @State private var searchText = ""
    @State private var searchResults: [SearchResult] = []

    var body: some View {
        NavigationStack {
            List {
                if searchResults.isEmpty && !searchText.isEmpty {
                    Text("Ничего не найдено")
                        .foregroundColor(TeleportTheme.textSecondary)
                } else {
                    ForEach(searchResults) { result in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(result.chatTitle)
                                .font(.subheadline.bold())
                            Text(result.messageText)
                                .font(.body)
                                .foregroundColor(TeleportTheme.textSecondary)
                                .lineLimit(2)
                            Text(result.date)
                                .font(.caption)
                                .foregroundColor(TeleportTheme.textSecondary)
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .listStyle(.plain)
            .searchable(text: $searchText, prompt: "Поиск сообщений")
            .navigationTitle("Поиск")
            .onChange(of: searchText) { query in
                performSearch(query: query)
            }
        }
    }

    private func performSearch(query: String) {
        // TODO: real search across messages
        guard !query.isEmpty else { searchResults = []; return }
        searchResults = [
            SearchResult(id: "1", chatTitle: "Алексей", messageText: "Текст с \"\(query)\"...", date: "сегодня"),
            SearchResult(id: "2", chatTitle: "Работа", messageText: "Обсуждали \"\(query)\"", date: "вчера"),
        ]
    }
}

struct SearchResult: Identifiable {
    let id: String
    let chatTitle: String
    let messageText: String
    let date: String
}
