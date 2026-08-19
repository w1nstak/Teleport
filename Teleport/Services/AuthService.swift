import Foundation
import SwiftUI

class AuthService: ObservableObject {
    @Published var isAuthenticated = false
    @Published var currentUser: TeleportUser?
    @Published var isLoading = false
    @Published var errorMessage: String?

    func login(phone: String, code: String) {
        isLoading = true
        // TODO: Replace with real backend auth (Firebase/Supabase)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.currentUser = TeleportUser(
                id: UUID().uuidString,
                phone: phone,
                displayName: "User",
                isOnline: true
            )
            self.isAuthenticated = true
            self.isLoading = false
        }
    }

    func sendCode(phone: String) {
        // TODO: Send SMS verification code
    }

    func logout() {
        isAuthenticated = false
        currentUser = nil
    }
}
