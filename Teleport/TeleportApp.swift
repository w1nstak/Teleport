import SwiftUI

@main
struct TeleportApp: App {
    @StateObject private var tdlib = TDLibManager.shared
    @StateObject private var authService = AuthService()

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
                        .environmentObject(authService)
                case .loggingOut, .closed:
                    TelegramLoginView()
                        .environmentObject(tdlib)
                }
            }
        }
    }
}
