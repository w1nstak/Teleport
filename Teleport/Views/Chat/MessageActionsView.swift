import SwiftUI

struct MessageActionsView: View {
    let message: Message
    var onReply: () -> Void
    var onForward: () -> Void
    var onEdit: () -> Void
    var onDelete: () -> Void
    var onPin: () -> Void
    var onReact: (String) -> Void

    private let quickReactions = ["👍", "❤️", "🔥", "😂", "😮", "😢"]

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 16) {
                ForEach(quickReactions, id: \.self) { emoji in
                    Button(emoji) { onReact(emoji) }
                        .font(.title2)
                }
            }
            .padding()

            Divider()

            VStack(spacing: 0) {
                ActionButton(icon: "arrowshape.turn.up.left.fill", title: "Ответить", action: onReply)
                ActionButton(icon: "arrowshape.turn.up.right.fill", title: "Переслать", action: onForward)
                ActionButton(icon: "doc.on.doc.fill", title: "Копировать", action: {})
                if message.senderId == "me" {
                    ActionButton(icon: "pencil", title: "Редактировать", action: onEdit)
                }
                ActionButton(icon: "pin.fill", title: "Закрепить", action: onPin)
                ActionButton(icon: "trash.fill", title: "Удалить", isDestructive: true, action: onDelete)
            }
        }
        .background(TeleportTheme.backgroundColor)
        .cornerRadius(16)
    }
}

struct ActionButton: View {
    let icon: String
    let title: String
    var isDestructive: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: icon)
                    .frame(width: 24)
                Text(title)
                Spacer()
            }
            .padding(.horizontal)
            .padding(.vertical, 12)
            .foregroundColor(isDestructive ? .red : TeleportTheme.textPrimary)
        }
    }
}
