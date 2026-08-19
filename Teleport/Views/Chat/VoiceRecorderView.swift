import SwiftUI

struct VoiceRecorderView: View {
    @Binding var isRecording: Bool
    @State private var recordingDuration: TimeInterval = 0
    @State private var timer: Timer?

    var onSend: (TimeInterval) -> Void
    var onCancel: () -> Void

    var body: some View {
        HStack(spacing: 16) {
            Button(action: {
                onCancel()
                stopRecording()
            }) {
                Image(systemName: "xmark.circle.fill")
                    .font(.title2)
                    .foregroundColor(.red)
            }

            HStack(spacing: 8) {
                Circle()
                    .fill(.red)
                    .frame(width: 10, height: 10)
                Text(formatDuration(recordingDuration))
                    .font(.body.monospacedDigit())
                    .foregroundColor(TeleportTheme.textPrimary)
            }

            Spacer()

            Button(action: {
                onSend(recordingDuration)
                stopRecording()
            }) {
                Image(systemName: "arrow.up.circle.fill")
                    .font(.title)
                    .foregroundColor(TeleportTheme.primaryColor)
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
        .background(TeleportTheme.backgroundColor)
        .onAppear { startRecording() }
        .onDisappear { stopRecording() }
    }

    private func startRecording() {
        recordingDuration = 0
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            recordingDuration += 1
        }
    }

    private func stopRecording() {
        timer?.invalidate()
        timer = nil
        isRecording = false
    }

    private func formatDuration(_ duration: TimeInterval) -> String {
        let minutes = Int(duration) / 60
        let seconds = Int(duration) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }
}
