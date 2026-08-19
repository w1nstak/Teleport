import SwiftUI

struct StorageSettingsView: View {
    @State private var keepMedia = "Всегда"
    @State private var autoDownloadPhotos = true
    @State private var autoDownloadVideos = false
    @State private var autoDownloadFiles = false

    private let keepOptions = ["3 дня", "1 неделя", "1 месяц", "Всегда"]

    var body: some View {
        Form {
            Section("Использование") {
                HStack {
                    Text("Сообщения")
                    Spacer()
                    Text("12.4 МБ")
                        .foregroundColor(TeleportTheme.textSecondary)
                }
                HStack {
                    Text("Медиа")
                    Spacer()
                    Text("156 МБ")
                        .foregroundColor(TeleportTheme.textSecondary)
                }
                HStack {
                    Text("Кэш")
                    Spacer()
                    Text("43 МБ")
                        .foregroundColor(TeleportTheme.textSecondary)
                }
                Button("Очистить кэш") {}
                    .foregroundColor(.red)
            }

            Section("Хранение медиа") {
                Picker("Хранить медиа", selection: $keepMedia) {
                    ForEach(keepOptions, id: \.self) { Text($0) }
                }
            }

            Section("Автозагрузка") {
                Toggle("Фото", isOn: $autoDownloadPhotos)
                Toggle("Видео", isOn: $autoDownloadVideos)
                Toggle("Файлы", isOn: $autoDownloadFiles)
            }
        }
        .navigationTitle("Хранилище")
    }
}
