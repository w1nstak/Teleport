import SwiftUI

struct CreateGroupView: View {
    @Binding var isPresented: Bool
    @State private var groupName = ""
    @State private var selectedContacts: Set<Int> = []

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    Circle()
                        .fill(TeleportTheme.primaryColor.opacity(0.15))
                        .frame(width: 56, height: 56)
                        .overlay(
                            Image(systemName: "camera.fill")
                                .foregroundColor(TeleportTheme.primaryColor)
                        )
                    TextField("Название группы", text: $groupName)
                        .font(.headline)
                }
                .padding()

                Divider()

                List {
                    Section("Добавить участников") {
                        ForEach(0..<10) { i in
                            HStack {
                                Circle()
                                    .fill(TeleportTheme.primaryColor.opacity(0.1))
                                    .frame(width: 40, height: 40)
                                    .overlay(Image(systemName: "person.fill").foregroundColor(TeleportTheme.primaryColor))
                                Text("Контакт \(i + 1)")
                                Spacer()
                                if selectedContacts.contains(i) {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(TeleportTheme.primaryColor)
                                }
                            }
                            .contentShape(Rectangle())
                            .onTapGesture {
                                if selectedContacts.contains(i) {
                                    selectedContacts.remove(i)
                                } else {
                                    selectedContacts.insert(i)
                                }
                            }
                        }
                    }
                }
                .listStyle(.plain)
            }
            .navigationTitle("Новая группа")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Отмена") { isPresented = false }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Создать") { isPresented = false }
                        .disabled(groupName.isEmpty || selectedContacts.isEmpty)
                        .bold()
                }
            }
        }
    }
}
