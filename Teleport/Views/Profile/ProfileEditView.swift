import SwiftUI

struct ProfileEditView: View {
    @EnvironmentObject var authService: AuthService
    @State private var displayName: String = ""
    @State private var bio: String = ""
    @State private var username: String = ""
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    HStack {
                        Spacer()
                        VStack(spacing: 8) {
                            Circle()
                                .fill(TeleportTheme.primaryColor.opacity(0.2))
                                .frame(width: 80, height: 80)
                                .overlay(
                                    Image(systemName: "camera.fill")
                                        .foregroundColor(TeleportTheme.primaryColor)
                                )
                            Text("Изменить фото")
                                .font(.caption)
                                .foregroundColor(TeleportTheme.primaryColor)
                        }
                        Spacer()
                    }
                }

                Section("Имя") {
                    TextField("Имя", text: $displayName)
                }

                Section("Имя пользователя") {
                    TextField("@username", text: $username)
                        .autocapitalization(.none)
                }

                Section("О себе") {
                    TextEditor(text: $bio)
                        .frame(minHeight: 80)
                }

                Section("Телефон") {
                    Text(authService.currentUser?.phone ?? "")
                        .foregroundColor(TeleportTheme.textSecondary)
                }
            }
            .navigationTitle("Редактировать")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Отмена") { dismiss() }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Готово") {
                        authService.currentUser?.displayName = displayName
                        authService.currentUser?.bio = bio.isEmpty ? nil : bio
                        dismiss()
                    }
                    .bold()
                }
            }
            .onAppear {
                displayName = authService.currentUser?.displayName ?? ""
                bio = authService.currentUser?.bio ?? ""
            }
        }
    }
}
