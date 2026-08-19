import SwiftUI

struct StickerPickerView: View {
    @Binding var isPresented: Bool
    var onStickerSelected: (Sticker) -> Void

    @State private var selectedTab = 0
    @State private var searchText = ""

    private let mockPacks: [StickerPack] = [
        StickerPack(id: "1", title: "Смайлики", stickers: (1...20).map {
            Sticker(id: "s\($0)", emoji: ["😀","😂","🥰","😎","🤔","👍","🔥","❤️","💀","🎉","😱","🤯","🥳","😤","🙄","💪","👀","🫡","🤝","✨"][$0-1], imageURL: "", isAnimated: false)
        }, isAnimated: false),
        StickerPack(id: "2", title: "Котики", stickers: (1...12).map {
            Sticker(id: "c\($0)", emoji: "🐱", imageURL: "", isAnimated: false)
        }, isAnimated: false),
    ]

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                TextField("Поиск стикеров", text: $searchText)
                    .padding(8)
                    .background(TeleportTheme.secondaryBackground)
                    .cornerRadius(8)
                Button("Закрыть") { isPresented = false }
            }
            .padding(.horizontal)
            .padding(.top, 8)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(mockPacks.indices, id: \.self) { i in
                        Button(action: { selectedTab = i }) {
                            Text(mockPacks[i].title)
                                .font(.caption)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(selectedTab == i ? TeleportTheme.primaryColor.opacity(0.15) : Color.clear)
                                .cornerRadius(12)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }

            ScrollView {
                LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 5), spacing: 8) {
                    ForEach(mockPacks[selectedTab].stickers) { sticker in
                        Button(action: { onStickerSelected(sticker) }) {
                            Text(sticker.emoji)
                                .font(.largeTitle)
                                .frame(width: 50, height: 50)
                        }
                    }
                }
                .padding()
            }
        }
        .frame(height: 300)
        .background(TeleportTheme.backgroundColor)
    }
}
