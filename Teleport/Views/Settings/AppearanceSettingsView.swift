import SwiftUI

struct AppearanceSettingsView: View {
    @State private var selectedTheme = 0
    @State private var fontSize: Double = 16
    @State private var chatBackground = 0

    private let themes = ["Системная", "Светлая", "Тёмная"]
    private let backgrounds = ["По умолчанию", "Градиент", "Однотонный", "Своё фото"]

    var body: some View {
        Form {
            Section("Тема") {
                Picker("Оформление", selection: $selectedTheme) {
                    ForEach(themes.indices, id: \.self) { Text(themes[$0]) }
                }
                .pickerStyle(.segmented)
            }

            Section("Размер текста") {
                HStack {
                    Text("A")
                        .font(.caption)
                    Slider(value: $fontSize, in: 12...24, step: 1)
                    Text("A")
                        .font(.title2)
                }
            }

            Section("Фон чата") {
                Picker("Фон", selection: $chatBackground) {
                    ForEach(backgrounds.indices, id: \.self) { Text(backgrounds[$0]) }
                }
            }

            Section {
                HStack {
                    Text("Предпросмотр")
                        .font(.subheadline)
                    Spacer()
                }

                VStack(spacing: 8) {
                    HStack {
                        Text("Привет! Как дела?")
                            .font(.system(size: fontSize))
                            .padding(10)
                            .background(TeleportTheme.bubbleIncoming)
                            .cornerRadius(16)
                        Spacer()
                    }
                    HStack {
                        Spacer()
                        Text("Всё отлично!")
                            .font(.system(size: fontSize))
                            .foregroundColor(.white)
                            .padding(10)
                            .background(TeleportTheme.bubbleOutgoing)
                            .cornerRadius(16)
                    }
                }
                .padding(.vertical, 8)
            }
        }
        .navigationTitle("Оформление")
    }
}
