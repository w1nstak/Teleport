import SwiftUI

struct CreateChannelView: View {
    @Binding var isPresented: Bool
    @State private var channelName = ""
    @State private var channelDescription = ""
    @State private var isPublic = true
    @State private var username = ""

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    HStack {
                        Spacer()
                        Circle()
                            .fill(TeleportTheme.primaryColor.opacity(0.15))
                            .frame(width: 72, height: 72)
                            .overlay(
                                Image(systemName: "camera.fill")
                                    .font(.title2)
                                    .foregroundColor(TeleportTheme.primaryColor)
                            )
                        Spacer()
                    }
                }

                Section("Информация") {
                    TextField("Название канала", text: $channelName)
                    TextField("Описание (необязательно)", text: $channelDescription)
                }

                Section("Тип") {
                    Toggle("Публичный канал", isOn: $isPublic)
                    if isPublic {
                        TextField("@username", text: $username)
                            .autocapitalization(.none)
                    }
                }
            }
            .navigationTitle("Новый канал")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Отмена") { isPresented = false }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Создать") { isPresented = false }
                        .disabled(channelName.isEmpty)
                        .bold()
                }
            }
        }
    }
}
