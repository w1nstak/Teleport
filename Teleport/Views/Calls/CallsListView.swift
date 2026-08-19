import SwiftUI

struct CallsListView: View {
    @State private var calls: [Call] = Call.mockCalls

    var body: some View {
        NavigationStack {
            List {
                ForEach(calls) { call in
                    CallRowView(call: call)
                }
            }
            .listStyle(.plain)
            .navigationTitle("Звонки")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {}) {
                        Image(systemName: "phone.badge.plus")
                    }
                }
            }
        }
    }
}

struct CallRowView: View {
    let call: Call

    var body: some View {
        HStack(spacing: 12) {
            Circle()
                .fill(TeleportTheme.primaryColor.opacity(0.15))
                .frame(width: 44, height: 44)
                .overlay(
                    Image(systemName: "person.fill")
                        .foregroundColor(TeleportTheme.primaryColor)
                )

            VStack(alignment: .leading, spacing: 4) {
                Text("Пользователь")
                    .font(.body)
                HStack(spacing: 4) {
                    Image(systemName: call.status == .missed ? "phone.arrow.down.left" : "phone.arrow.up.right")
                        .font(.caption)
                        .foregroundColor(call.status == .missed ? .red : .green)
                    Text(call.type == .video ? "Видеозвонок" : "Голосовой")
                        .font(.caption)
                        .foregroundColor(TeleportTheme.textSecondary)
                }
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 4) {
                Text(dateString(call.startedAt ?? Date()))
                    .font(.caption)
                    .foregroundColor(TeleportTheme.textSecondary)
                Image(systemName: call.type == .video ? "video.fill" : "phone.fill")
                    .foregroundColor(TeleportTheme.primaryColor)
            }
        }
        .padding(.vertical, 4)
    }

    private func dateString(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd.MM, HH:mm"
        return formatter.string(from: date)
    }
}

extension Call {
    static let mockCalls: [Call] = [
        Call(id: "c1", callerId: "me", receiverId: "u2", type: .voice, status: .ended, startedAt: Date().addingTimeInterval(-3600), endedAt: Date().addingTimeInterval(-3500), duration: 100),
        Call(id: "c2", callerId: "u3", receiverId: "me", type: .video, status: .missed, startedAt: Date().addingTimeInterval(-7200)),
        Call(id: "c3", callerId: "me", receiverId: "u4", type: .voice, status: .ended, startedAt: Date().addingTimeInterval(-86400), endedAt: Date().addingTimeInterval(-86100), duration: 300),
    ]
}
