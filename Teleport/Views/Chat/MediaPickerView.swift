import SwiftUI
import PhotosUI

struct MediaPickerView: View {
    @Binding var isPresented: Bool
    var onPhotoPicked: (UIImage) -> Void
    var onFilePicked: (URL) -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("Вложение")
                    .font(.headline)
                Spacer()
                Button("Закрыть") { isPresented = false }
            }
            .padding()

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                AttachmentOption(icon: "photo.fill", title: "Фото", color: .blue) {}
                AttachmentOption(icon: "video.fill", title: "Видео", color: .red) {}
                AttachmentOption(icon: "doc.fill", title: "Файл", color: .orange) {}
                AttachmentOption(icon: "location.fill", title: "Геопозиция", color: .green) {}
                AttachmentOption(icon: "person.fill", title: "Контакт", color: .purple) {}
                AttachmentOption(icon: "chart.bar.fill", title: "Опрос", color: .teal) {}
            }
            .padding()

            Spacer()
        }
        .background(TeleportTheme.backgroundColor)
    }
}

struct AttachmentOption: View {
    let icon: String
    let title: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Circle()
                    .fill(color.opacity(0.15))
                    .frame(width: 56, height: 56)
                    .overlay(
                        Image(systemName: icon)
                            .font(.title3)
                            .foregroundColor(color)
                    )
                Text(title)
                    .font(.caption)
                    .foregroundColor(TeleportTheme.textPrimary)
            }
        }
    }
}
