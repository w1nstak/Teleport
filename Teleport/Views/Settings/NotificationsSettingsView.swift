import SwiftUI

struct NotificationsSettingsView: View {
    @State private var messagesEnabled = true
    @State private var groupsEnabled = true
    @State private var channelsEnabled = true
    @State private var soundEnabled = true
    @State private var vibrationEnabled = true
    @State private var previewEnabled = true

    var body: some View {
        Form {
            Section("Сообщения") {
                Toggle("Личные сообщения", isOn: $messagesEnabled)
                Toggle("Группы", isOn: $groupsEnabled)
                Toggle("Каналы", isOn: $channelsEnabled)
            }

            Section("Оповещения") {
                Toggle("Звук", isOn: $soundEnabled)
                Toggle("Вибрация", isOn: $vibrationEnabled)
                Toggle("Предпросмотр", isOn: $previewEnabled)
            }

            Section {
                Button("Сбросить все уведомления") {}
                    .foregroundColor(.red)
            }
        }
        .navigationTitle("Уведомления")
    }
}
