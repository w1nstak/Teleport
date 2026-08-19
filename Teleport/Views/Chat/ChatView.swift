import SwiftUI

struct ChatView: View {
    let chat: Chat
    @StateObject private var messageService = MessageService()
    @State private var inputText = ""
    @State private var showMediaPicker = false
    @State private var showStickerPicker = false
    @State private var isRecording = false
    @State private var replyingTo: Message?
    @State private var selectedMessage: Message?
    @State private var showMessageActions = false

    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(messageService.messages) { message in
                            MessageBubbleView(message: message, isMe: message.senderId == "me")
                                .id(message.id)
                                .onLongPressGesture {
                                    selectedMessage = message
                                    showMessageActions = true
                                }
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                }
                .onChange(of: messageService.messages.count) { _ in
                    if let last = messageService.messages.last {
                        withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                    }
                }
            }

            Divider()

            if let reply = replyingTo {
                HStack {
                    Rectangle()
                        .fill(TeleportTheme.primaryColor)
                        .frame(width: 3)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Ответ")
                            .font(.caption.bold())
                            .foregroundColor(TeleportTheme.primaryColor)
                        Text(reply.text ?? "")
                            .font(.caption)
                            .foregroundColor(TeleportTheme.textSecondary)
                            .lineLimit(1)
                    }
                    Spacer()
                    Button(action: { replyingTo = nil }) {
                        Image(systemName: "xmark")
                            .foregroundColor(TeleportTheme.textSecondary)
                    }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(TeleportTheme.secondaryBackground)
            }

            if isRecording {
                VoiceRecorderView(isRecording: $isRecording, onSend: { _ in }, onCancel: {})
            } else {
                inputBar
            }
        }
        .navigationTitle(chat.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack(spacing: 16) {
                    Button(action: {}) {
                        Image(systemName: "phone.fill")
                    }
                    Button(action: {}) {
                        Image(systemName: "video.fill")
                    }
                }
            }
        }
        .onAppear { messageService.loadMessages(chatId: chat.id) }
        .sheet(isPresented: $showMediaPicker) {
            MediaPickerView(isPresented: $showMediaPicker, onPhotoPicked: { _ in }, onFilePicked: { _ in })
                .presentationDetents([.medium])
        }
        .sheet(isPresented: $showStickerPicker) {
            StickerPickerView(isPresented: $showStickerPicker, onStickerSelected: { _ in })
                .presentationDetents([.medium])
        }
        .sheet(isPresented: $showMessageActions) {
            if let msg = selectedMessage {
                MessageActionsView(
                    message: msg,
                    onReply: { replyingTo = msg; showMessageActions = false },
                    onForward: { showMessageActions = false },
                    onEdit: { showMessageActions = false },
                    onDelete: { showMessageActions = false },
                    onPin: { showMessageActions = false },
                    onReact: { _ in showMessageActions = false }
                )
                .presentationDetents([.medium])
            }
        }
    }

    private var inputBar: some View {
        HStack(spacing: 10) {
            Button(action: { showMediaPicker = true }) {
                Image(systemName: "paperclip")
                    .font(.title3)
                    .foregroundColor(TeleportTheme.textSecondary)
            }

            TextField("Сообщение", text: $inputText)
                .padding(10)
                .background(TeleportTheme.secondaryBackground)
                .cornerRadius(20)

            Button(action: { showStickerPicker = true }) {
                Image(systemName: "face.smiling")
                    .font(.title3)
                    .foregroundColor(TeleportTheme.textSecondary)
            }

            if inputText.isEmpty {
                Button(action: { isRecording = true }) {
                    Image(systemName: "mic.fill")
                        .font(.title3)
                        .foregroundColor(TeleportTheme.primaryColor)
                }
            } else {
                Button(action: sendMessage) {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.title)
                        .foregroundColor(TeleportTheme.primaryColor)
                }
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(TeleportTheme.backgroundColor)
    }

    private func sendMessage() {
        guard !inputText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        messageService.sendMessage(chatId: chat.id, senderId: "me", text: inputText)
        inputText = ""
        replyingTo = nil
    }
}

struct MessageBubbleView: View {
    let message: Message
    let isMe: Bool

    var body: some View {
        HStack {
            if isMe { Spacer(minLength: 60) }

            VStack(alignment: isMe ? .trailing : .leading, spacing: 4) {
                if let text = message.text {
                    Text(text)
                        .font(.body)
                        .foregroundColor(isMe ? .white : TeleportTheme.textPrimary)
                }
                HStack(spacing: 4) {
                    if message.isEdited {
                        Text("ред.")
                            .font(.caption2)
                    }
                    Text(timeString(message.createdAt))
                        .font(.caption2)
                    if isMe {
                        Image(systemName: message.isRead ? "checkmark.circle.fill" : "checkmark.circle")
                            .font(.caption2)
                    }
                }
                .foregroundColor(isMe ? .white.opacity(0.7) : TeleportTheme.textSecondary)
            }
            .padding(.horizontal, TeleportTheme.messagePadding)
            .padding(.vertical, 8)
            .background(isMe ? TeleportTheme.bubbleOutgoing : TeleportTheme.bubbleIncoming)
            .cornerRadius(16)

            if !isMe { Spacer(minLength: 60) }
        }
    }

    private func timeString(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
}
