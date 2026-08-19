import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var authService: AuthService

    var body: some View {
        NavigationStack {
            List {
                Section {
                    HStack(spacing: 14) {
                        Circle()
                            .fill(TeleportTheme.primaryColor.opacity(0.2))
                            .frame(width: 60, height: 60)
                            .overlay(
                                Image(systemName: "person.fill")
                                    .font(.title2)
                                    .foregroundColor(TeleportTheme.primaryColor)
                            )
                        VStack(alignment: .leading, spacing: 4) {
                            Text(authService.currentUser?.displayName ?? "User")
                                .font(.headline)
                            Text(authService.currentUser?.phone ?? "")
                                .font(.subheadline)
                                .foregroundColor(TeleportTheme.textSecondary)
                        }
                    }
                }

                Section("Основные") {
                    NavigationLink { NotificationsSettingsView() } label: {
                        Label("Уведомления", systemImage: "bell.fill")
                    }
                    NavigationLink { PrivacySettingsView() } label: {
                        Label("Конфиденциальность", systemImage: "lock.fill")
                    }
                    NavigationLink { AppearanceSettingsView() } label: {
                        Label("Оформление", systemImage: "paintbrush.fill")
                    }
                    NavigationLink { StorageSettingsView() } label: {
                        Label("Хранилище", systemImage: "internaldrive.fill")
                    }
                }

                Section {
                    Button(role: .destructive) {
                        authService.logout()
                    } label: {
                        Label("Выйти", systemImage: "rectangle.portrait.and.arrow.right")
                    }
                }
            }
            .navigationTitle("Настройки")
        }
    }
}
