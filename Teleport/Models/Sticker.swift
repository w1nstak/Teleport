import Foundation

struct StickerPack: Identifiable, Codable {
    let id: String
    var title: String
    var stickers: [Sticker]
    var thumbnailURL: String?
    var isAnimated: Bool
}

struct Sticker: Identifiable, Codable {
    let id: String
    var emoji: String
    var imageURL: String
    var isAnimated: Bool
}
