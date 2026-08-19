import SwiftUI

struct LoginView: View {
    @EnvironmentObject var authService: AuthService
    @StateObject private var telegram = TelegramService()
    @State private var phone = ""
    @State private var code = ""
    @State private var password = ""

    var body: some View {
        VStack(spacing: 32) {
            Spacer()

            Image(systemName: "paperplane.fill")
                .font(.system(size: 64))
                .foregroundColor(TeleportTheme.primaryColor)

            Text("Teleport")
                .font(.largeTitle.bold())
                .foregroundColor(TeleportTheme.textPrimary)

            Text("Войди со своим аккаунтом Telegram")
                .font(.subheadline)
                .foregroundColor(TeleportTheme.textSecondary)

            VStack(spacing: 16) {
                switch telegram.authState {
                case .waitingPhone:
                    TextField("Номер телефона", text: $phone)
                        .keyboardType(.phonePad)
                        .padding()
                        .background(TeleportTheme.secondaryBackground)
                        .cornerRadius(TeleportTheme.cornerRadius)

                case .waitingCode:
                    TextField("Код из Telegram", text: $code)
                        .keyboardType(.numberPad)
                        .padding()
                        .background(TeleportTheme.secondaryBackground)
                        .cornerRadius(TeleportTheme.cornerRadius)

                case .waitingPassword:
                    SecureField("Пароль двухфакторной аутентификации", text: $password)
                        .padding()
                        .background(TeleportTheme.secondaryBackground)
                        .cornerRadius(TeleportTheme.cornerRadius)

                case .ready:
                    ProgressView("Загрузка чатов...")
                }
            }
            .padding(.horizontal)

            Button(action: handleAction) {
                Text(buttonTitle)
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(TeleportTheme.primaryColor)
                    .cornerRadius(TeleportTheme.cornerRadius)
            }
            .padding(.horizontal)
            .disabled(isButtonDisabled)

            Spacer()
        }
        .background(TeleportTheme.backgroundColor.ignoresSafeArea())
        .onChange(of: telegram.isAuthorized) { authorized in
            if authorized {
                authService.isAuthenticated = true
            }
        }
    }

    private var buttonTitle: String {
        switch telegram.authState {
        case .waitingPhone: return "Далее"
        case .waitingCode: return "Подтвердить код"
        case .waitingPassword: return "Войти"
        case .ready: return "Готово"
        }
    }

    private var isButtonDisabled: Bool {
        switch telegram.authState {
        case .waitingPhone: return phone.isEmpty
        case .waitingCode: return code.isEmpty
        case .waitingPassword: return password.isEmpty
        case .ready: return true
        }
    }

    private func handleAction() {
        switch telegram.authState {
        case .waitingPhone:
            telegram.sendPhone(phone)
        case .waitingCode:
            telegram.sendCode(code)
        case .waitingPassword:
            telegram.sendPassword(password)
        case .ready:
            break
        }
    }
}
