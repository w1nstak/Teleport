import Foundation
import SwiftUI

class AuthService: ObservableObject {
    @Published var isAuthenticated = false
    @Published var currentUser: TeleportUser?
    @Published var isLoading = false
    @Published var errorMessage: String?

    func logout() {
        isAuthenticated = false
        currentUser = nil
    }
}
