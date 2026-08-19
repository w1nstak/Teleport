import SwiftUI

struct ContactsView: View {
    var body: some View {
        NavigationStack {
            List {
                ForEach(0..<10) { i in
                    HStack(spacing: 12) {
                        Circle()
                            .fill(TeleportTheme.primaryColor.opacity(0.15))
                            .frame(width: 44, height: 44)
                            .overlay(
                                Image(systemName: "person.fill")
                                    .foregroundColor(TeleportTheme.primaryColor)
                            )
                        VStack(alignment: .leading) {
                            Text("Контакт \(i + 1)")
                                .font(.body)
                            Text("был(а) недавно")
                                .font(.caption)
                                .foregroundColor(TeleportTheme.textSecondary)
                        }
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("Контакты")
        }
    }
}
