import SwiftUI

struct CallScreenView: View {
    let userName: String
    let callType: Call.CallType
    @State private var isMuted = false
    @State private var isSpeaker = false
    @State private var isVideoOff = false
    @Binding var isPresented: Bool
    @State private var callDuration: TimeInterval = 0
    @State private var timer: Timer?

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            VStack(spacing: 32) {
                Spacer()

                Circle()
                    .fill(TeleportTheme.primaryColor.opacity(0.2))
                    .frame(width: 100, height: 100)
                    .overlay(
                        Text(String(userName.prefix(1)))
                            .font(.largeTitle.bold())
                            .foregroundColor(TeleportTheme.primaryColor)
                    )

                VStack(spacing: 8) {
                    Text(userName)
                        .font(.title2.bold())
                        .foregroundColor(.white)
                    Text(formatDuration(callDuration))
                        .font(.body.monospacedDigit())
                        .foregroundColor(.white.opacity(0.7))
                }

                Spacer()

                HStack(spacing: 40) {
                    CallControlButton(icon: isMuted ? "mic.slash.fill" : "mic.fill", isActive: isMuted) {
                        isMuted.toggle()
                    }
                    if callType == .video {
                        CallControlButton(icon: isVideoOff ? "video.slash.fill" : "video.fill", isActive: isVideoOff) {
                            isVideoOff.toggle()
                        }
                    }
                    CallControlButton(icon: "speaker.wave.2.fill", isActive: isSpeaker) {
                        isSpeaker.toggle()
                    }
                }

                Button(action: { isPresented = false }) {
                    Circle()
                        .fill(.red)
                        .frame(width: 64, height: 64)
                        .overlay(
                            Image(systemName: "phone.down.fill")
                                .foregroundColor(.white)
                                .font(.title2)
                        )
                }
                .padding(.bottom, 40)
            }
        }
        .onAppear {
            timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
                callDuration += 1
            }
        }
        .onDisappear { timer?.invalidate() }
    }

    private func formatDuration(_ d: TimeInterval) -> String {
        let m = Int(d) / 60
        let s = Int(d) % 60
        return String(format: "%02d:%02d", m, s)
    }
}

struct CallControlButton: View {
    let icon: String
    let isActive: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Circle()
                .fill(isActive ? Color.white.opacity(0.3) : Color.white.opacity(0.1))
                .frame(width: 52, height: 52)
                .overlay(
                    Image(systemName: icon)
                        .foregroundColor(.white)
                        .font(.title3)
                )
        }
    }
}
