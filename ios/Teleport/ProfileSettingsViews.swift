import SwiftUI

struct ProfileTabView: View {
    @EnvironmentObject var vm: AppViewModel
    @State private var showEdit = false
    @State private var showAppearance = false

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                ZStack(alignment: .bottom) {
                    Rectangle()
                        .fill(LinearGradient(colors: [Color.orange.opacity(0.6), Color.black], startPoint: .top, endPoint: .bottom))
                        .frame(height: 220)
                    VStack(spacing: 8) {
                        AvatarView(name: vm.user?.displayName ?? "?", size: 88)
                        HStack(spacing: 6) {
                            Text((vm.user?.displayName ?? "").uppercased())
                                .font(.title2.bold())
                            Image(systemName: "checkmark.seal.fill")
                                .foregroundStyle(SlimColors.accentBlue)
                        }
                        HStack(spacing: 6) {
                            Circle().fill(SlimColors.accentGreen).frame(width: 8, height: 8)
                            Text("в сети").foregroundStyle(SlimColors.accentGreen)
                        }
                        if let u = vm.user?.username {
                            Text("@\(u)").foregroundStyle(SlimColors.textSecondary)
                        }
                    }
                    .padding(.bottom, 16)
                }
                .overlay(alignment: .topLeading) {
                    ShareLink(item: shareText) {
                        Label("Поделиться", systemImage: "square.and.arrow.up")
                            .font(.caption)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(.black.opacity(0.45))
                            .clipShape(Capsule())
                    }
                    .padding()
                }

                VStack(alignment: .leading, spacing: 20) {
                    sectionHeader("О себе")
                    SlimGroupCard {
                        Text(vm.user?.bio?.isEmpty == false ? (vm.user?.bio ?? "") : "Добавьте информацию о себе")
                            .foregroundStyle(SlimColors.textPrimary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(16)
                    }

                    SlimGroupCard {
                        profileRow(icon: "phone.fill", color: SlimColors.accentGreen, title: "Номер", value: "Скрыт")
                        Divider().background(SlimColors.divider).padding(.leading, 56)
                        profileRow(icon: "at", color: SlimColors.accentBlue, title: "Имя пользователя", value: vm.user?.username.map { "@\($0)" } ?? "—")
                    }

                    sectionHeader("ОФОРМЛЕНИЕ")
                    SlimGroupCard {
                        HStack {
                            SettingsIconBox(systemName: "textformat", color: .red)
                            VStack(alignment: .leading) {
                                Text("Шрифт").foregroundStyle(SlimColors.textPrimary)
                                Text("Системный").font(.caption).foregroundStyle(SlimColors.textSecondary)
                            }
                            Spacer()
                            Button("Показать") { showAppearance = true }
                                .foregroundStyle(SlimColors.accentBlue)
                        }
                        .padding(16)
                    }

                    Button("Редактировать профиль") { showEdit = true }
                        .foregroundStyle(SlimColors.accentBlue)
                        .padding(.top, 8)
                }
                .padding(16)
            }
        }
        .background(SlimColors.screenBg)
        .navigationDestination(isPresented: $showEdit) { EditProfileView() }
        .navigationDestination(isPresented: $showAppearance) { AppearanceView() }
    }

    private var shareText: String {
        var s = vm.user?.displayName ?? ""
        if let u = vm.user?.username { s += "\n@\(u)" }
        return s
    }

    private func sectionHeader(_ text: String) -> some View {
        Text(text)
            .font(.caption)
            .foregroundStyle(SlimColors.textSecondary)
            .padding(.leading, 4)
    }

    private func profileRow(icon: String, color: Color, title: String, value: String) -> some View {
        HStack(spacing: 12) {
            SettingsIconBox(systemName: icon, color: color)
            VStack(alignment: .leading) {
                Text(title).font(.caption).foregroundStyle(SlimColors.textSecondary)
                Text(value).foregroundStyle(SlimColors.textPrimary)
            }
            Spacer()
        }
        .padding(16)
    }
}

struct SettingsTabView: View {
    @EnvironmentObject var vm: AppViewModel
    @State private var showAppearance = false
    @State private var showLocalization = false
    @State private var showEdit = false
    @State private var showPrivacy = false
    @State private var showFolders = false
    @State private var showStickers = false
    @State private var showDevices = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 22) {
                Text("Настройки")
                    .font(.system(size: 34, weight: .bold))
                    .padding(.leading, 4)

                SlimGroupCard {
                    settingsNavRow("star.fill", .blue, "Избранное") {
                        if let fav = vm.favoritesChat() {
                            // open favorites in chats tab — user switches manually
                        }
                    }
                }

                SlimGroupCard {
                    settingsNavRow("paintpalette.fill", Color(red: 0.69, green: 0.32, blue: 0.87), "Темы") { showAppearance = true }
                    divider
                    settingsNavRow("face.smiling", .yellow, "Эмодзи и стикеры") { showStickers = true }
                    divider
                    settingsNavRow("square.grid.2x2", Color(red: 0.35, green: 0.78, blue: 0.98), "Навигация") { showLocalization = true }
                    divider
                    settingsNavRow("person.fill", .blue, "Профиль") { showEdit = true }
                    divider
                    settingsNavRow("folder.fill", .orange, "Папки") { showFolders = true }
                    divider
                    settingsNavRow("lock.fill", SlimColors.accentGreen, "Конфиденциальность") { showPrivacy = true }
                    divider
                    settingsNavRow("globe", .blue, "Язык") { showLocalization = true }
                }

                SlimGroupCard {
                    settingsNavRow("desktopcomputer", Color(red: 0.35, green: 0.34, blue: 0.84), "Устройства") { showDevices = true }
                    divider
                    settingsToggleRow("bell.fill", .red, "Уведомления", $vm.settings.notificationsEnabled)
                    divider
                    settingsToggleRow("bolt.fill", SlimColors.accentGreen, "Энергосбережение", $vm.settings.powerSavingEnabled)
                    divider
                    settingsNavRow("hand.raised.fill", .orange, "Заблокированные") { showPrivacy = true }
                }

                Button("Выйти", role: .destructive) { vm.logout() }
                    .frame(maxWidth: .infinity)
                    .padding(.top, 8)
            }
            .padding(16)
        }
        .background(SlimColors.screenBg)
        .onChange(of: vm.settings.notificationsEnabled) { _, _ in vm.saveSettings() }
        .onChange(of: vm.settings.powerSavingEnabled) { _, _ in vm.saveSettings() }
        .navigationDestination(isPresented: $showAppearance) { AppearanceView() }
        .navigationDestination(isPresented: $showLocalization) { LocalizationView() }
        .navigationDestination(isPresented: $showEdit) { EditProfileView() }
        .navigationDestination(isPresented: $showPrivacy) { PrivacyView() }
        .navigationDestination(isPresented: $showFolders) { FoldersView() }
        .navigationDestination(isPresented: $showStickers) { StickersView() }
        .navigationDestination(isPresented: $showDevices) { DevicesView() }
    }

    private var divider: some View {
        Divider().background(SlimColors.divider).padding(.leading, 56)
    }

    private func settingsNavRow(_ icon: String, _ color: Color, _ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 12) {
                SettingsIconBox(systemName: icon, color: color)
                Text(title).foregroundStyle(SlimColors.textPrimary)
                Spacer()
                Image(systemName: "chevron.right").font(.caption).foregroundStyle(SlimColors.textSecondary)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
        .buttonStyle(.plain)
    }

    private func settingsToggleRow(_ icon: String, _ color: Color, _ title: String, _ binding: Binding<Bool>) -> some View {
        HStack(spacing: 12) {
            SettingsIconBox(systemName: icon, color: color)
            Text(title).foregroundStyle(SlimColors.textPrimary)
            Spacer()
            Toggle("", isOn: binding).labelsHidden()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
}

struct EditProfileView: View {
    @EnvironmentObject var vm: AppViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var username = ""
    @State private var bio = ""

    var body: some View {
        Form {
            TextField("Имя", text: $name)
            TextField("Username", text: $username)
            TextField("О себе", text: $bio, axis: .vertical)
            if let err = vm.errorMessage { Text(err).foregroundStyle(.red) }
            Button("Сохранить") {
                Task {
                    await vm.updateProfile(displayName: name, username: username, bio: bio)
                    dismiss()
                }
            }
        }
        .navigationTitle("Профиль")
        .onAppear {
            name = vm.user?.displayName ?? ""
            username = vm.user?.username ?? ""
            bio = vm.user?.bio ?? ""
        }
    }
}

struct AppearanceView: View {
    var body: some View {
        List {
            Text("Тема: SlimChat (тёмная)")
            Text("Системный шрифт")
        }
        .navigationTitle("Темы")
    }
}

struct LocalizationView: View {
    @EnvironmentObject var vm: AppViewModel
    private let keys = ["chats_title", "nav_chats", "nav_contacts", "nav_profile", "nav_settings", "nav_calls"]

    var body: some View {
        List {
            ForEach(keys, id: \.self) { key in
                HStack {
                    Text(key)
                    Spacer()
                    TextField("Надпись", text: binding(for: key))
                        .multilineTextAlignment(.trailing)
                }
            }
            Button("Сбросить") {
                vm.settings.localeOverrides = [:]
                vm.saveSettings()
            }
        }
        .navigationTitle("Локализация")
        .onDisappear { vm.saveSettings() }
    }

    private func binding(for key: String) -> Binding<String> {
        Binding(
            get: { vm.settings.localeOverrides[key] ?? str(key, overrides: [:]) },
            set: { vm.settings.localeOverrides[key] = $0 }
        )
    }
}

struct PrivacyView: View {
    var body: some View {
        List {
            Text("Номер телефона: Скрыт")
            Text("Статус «в сети»: Виден")
            Text("Заблокированные пользователи")
        }
        .navigationTitle("Конфиденциальность")
    }
}

struct FoldersView: View {
    var body: some View {
        List {
            Text("Все чаты")
            Text("Личные")
            Text("Группы")
        }
        .navigationTitle("Папки")
    }
}

struct StickersView: View {
    var body: some View {
        Text("Эмодзи и стикеры — скоро")
            .foregroundStyle(SlimColors.textSecondary)
            .navigationTitle("Стикеры")
    }
}

struct DevicesView: View {
    var body: some View {
        List {
            Section {
                HStack(spacing: 12) {
                    Image(systemName: "iphone")
                        .font(.title2)
                        .foregroundStyle(SlimColors.accentBlue)
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Это устройство")
                            .foregroundStyle(SlimColors.textPrimary)
                        Text("Активно")
                            .font(.caption)
                            .foregroundStyle(SlimColors.accentGreen)
                    }
                }
            }
            Section {
                Text("Приложение автоматически подключается к облачному серверу Teleport.")
                    .font(.caption)
                    .foregroundStyle(SlimColors.textSecondary)
            }
        }
        .scrollContentBackground(.hidden)
        .background(SlimColors.screenBg)
        .navigationTitle("Устройства")
    }
}
