import SwiftUI

struct ChatFoldersTabView: View {
    @State private var selectedFolder = 0

    private let folders: [ChatFolder] = [
        ChatFolder(id: "all", name: "Все", icon: "tray.fill", includedChatIds: [], filterRules: FolderFilter(includePrivate: true, includeGroups: true, includeChannels: true, includeBots: true, excludeMuted: false, excludeRead: false)),
        ChatFolder(id: "private", name: "Личные", icon: "person.fill", includedChatIds: [], filterRules: FolderFilter(includePrivate: true, includeGroups: false, includeChannels: false, includeBots: false, excludeMuted: false, excludeRead: false)),
        ChatFolder(id: "groups", name: "Группы", icon: "person.3.fill", includedChatIds: [], filterRules: FolderFilter(includePrivate: false, includeGroups: true, includeChannels: false, includeBots: false, excludeMuted: false, excludeRead: false)),
        ChatFolder(id: "channels", name: "Каналы", icon: "megaphone.fill", includedChatIds: [], filterRules: FolderFilter(includePrivate: false, includeGroups: false, includeChannels: true, includeBots: false, excludeMuted: false, excludeRead: false)),
        ChatFolder(id: "bots", name: "Боты", icon: "cpu", includedChatIds: [], filterRules: FolderFilter(includePrivate: false, includeGroups: false, includeChannels: false, includeBots: true, excludeMuted: false, excludeRead: false)),
    ]

    var body: some View {
        VStack(spacing: 0) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 0) {
                    ForEach(folders.indices, id: \.self) { i in
                        Button(action: { selectedFolder = i }) {
                            VStack(spacing: 4) {
                                Text(folders[i].name)
                                    .font(.subheadline)
                                    .fontWeight(selectedFolder == i ? .bold : .regular)
                                    .foregroundColor(selectedFolder == i ? TeleportTheme.primaryColor : TeleportTheme.textSecondary)
                                Rectangle()
                                    .fill(selectedFolder == i ? TeleportTheme.primaryColor : Color.clear)
                                    .frame(height: 2)
                            }
                            .padding(.horizontal, 16)
                        }
                    }
                }
            }
            .padding(.vertical, 4)

            Divider()
        }
    }
}
