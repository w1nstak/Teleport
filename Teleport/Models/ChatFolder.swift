import Foundation

struct ChatFolder: Identifiable, Codable {
    let id: String
    var name: String
    var icon: String
    var includedChatIds: [String]
    var filterRules: FolderFilter
}

struct FolderFilter: Codable {
    var includePrivate: Bool
    var includeGroups: Bool
    var includeChannels: Bool
    var includeBots: Bool
    var excludeMuted: Bool
    var excludeRead: Bool
}
