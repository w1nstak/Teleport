import SwiftUI

struct PrivacySettingsView: View {
    @State private var lastSeen = "Все"
    @State private var profilePhoto = "Все"
    @State private var forwarding = "Все"
    @State private var calls = "Все"
    @State private var groups = "Все"
    @State private var autoDeleteTimer = "Выкл"
    @State private var passcodeEnabled = false

    private let options = ["Все", "Мои контакты", "Никто"]
    private let deleteOptions = ["Выкл", "1 день", "7 дней", "1 месяц"]

    var body: some View {
        Form {
            Section("Кто может видеть") {
                Picker("Время посещения", selection: $lastSeen) {
                    ForEach(options, id: \.self) { Text($0) }
                }
                Picker("Фото профиля", selection: $profilePhoto) {
                    ForEach(options, id: \.self) { Text($0) }
                }
                Picker("Пересылку сообщений", selection: $forwarding) {
                    ForEach(options, id: \.self) { Text($0) }
                }
            }

            Section("Кто может") {
                Picker("Звонить мне", selection: $calls) {
                    ForEach(options, id: \.self) { Text($0) }
                }
                Picker("Добавлять в группы", selection: $groups) {
                    ForEach(options, id: \.self) { Text($0) }
                }
            }

            Section("Удаление сообщений") {
                Picker("Автоудаление", selection: $autoDeleteTimer) {
                    ForEach(deleteOptions, id: \.self) { Text($0) }
                }
            }

            Section("Безопасность") {
                Toggle("Код-пароль / Face ID", isOn: $passcodeEnabled)
                NavigationLink("Активные сессии") {}
                NavigationLink("Заблокированные") {}
            }
        }
        .navigationTitle("Конфиденциальность")
    }
}
