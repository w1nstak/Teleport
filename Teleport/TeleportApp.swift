import SwiftUI

@main
struct TeleportApp: App {
    @StateObject private var tdlib = TDLibManager.shared

    var body: some Scene {
        WindowGroup {
            Group {
                switch tdlib.authorizationState {
                case .waitingTdlibParameters:
                    ProgressView("Инициализация...")
                case .waitingPhoneNumber, .waitingCode, .waitingPassword:
                    TelegramLoginView()
                        .environmentObject(tdlib)
                case .ready:
                    MainTabView()
                        .environmentObject(tdlib)
                case .loggingOut, .closed:
                    TelegramLoginView()
                        .environmentObject(tdlib)
                }
            }
        }
    }
}
