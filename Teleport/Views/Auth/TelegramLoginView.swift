import SwiftUI

struct TelegramLoginView: View {
    @EnvironmentObject var tdlib: TDLibManager
    @State private var phone = ""
    @State private var code = ""
    @State private var password = ""
    @State private var isLoading = false

    var body: some View {
        VStack(spacing: 32) {
            Spacer()

            Image(systemName: "paperplane.fill")
                .font(.system(size: 64))
                .foregroundColor(TeleportTheme.primaryColor)

            Text("Teleport")
                .font(.largeTitle.bold())

            Text(subtitle)
                .font(.subheadline)
                .foregroundColor(TeleportTheme.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            VStack(spacing: 16) {
                switch tdlib.authorizationState {
                case .waitingPhoneNumber:
                    TextField("+7 999 123 45 67", text: $phone)
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
                    SecureField("Облачный пароль", text: $password)
                        .padding()
                        .background(TeleportTheme.secondaryBackground)
                        .cornerRadius(TeleportTheme.cornerRadius)

                default:
                    EmptyView()
                }
            }
            .padding(.horizontal, 32)

            Button(action: handleNext) {
                if isLoading {
                    ProgressView()
                        .tint(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                } else {
                    Text(buttonTitle)
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                }
            }
            .background(TeleportTheme.primaryColor)
            .cornerRadius(TeleportTheme.cornerRadius)
            .padding(.horizontal, 32)
            .disabled(isButtonDisabled)

            Spacer()
            Spacer()
        }
        .background(TeleportTheme.backgroundColor.ignoresSafeArea())
    }

    private var subtitle: String {
        switch tdlib.authorizationState {
        case .waitingPhoneNumber:
            return "Введи номер телефона от своего Telegram-аккаунта"
        case .waitingCode:
            return "Код отправлен в Telegram на другое устройство"
        case .waitingPassword:
            return "Введи пароль двухфакторной аутентификации"
        default:
            return ""
        }
    }

    private var buttonTitle: String {
        switch tdlib.authorizationState {
        case .waitingPhoneNumber: return "Далее"
        case .waitingCode: return "Подтвердить"
        case .waitingPassword: return "Войти"
        default: return "..."
        }
    }

    private var isButtonDisabled: Bool {
        if isLoading { return true }
        switch tdlib.authorizationState {
        case .waitingPhoneNumber: return phone.count < 5
        case .waitingCode: return code.count < 4
        case .waitingPassword: return password.isEmpty
        default: return true
        }
    }

    private func handleNext() {
        isLoading = true
        switch tdlib.authorizationState {
        case .waitingPhoneNumber:
            tdlib.setPhoneNumber(phone)
        case .waitingCode:
            tdlib.checkCode(code)
        case .waitingPassword:
            tdlib.checkPassword(password)
        default:
            break
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            isLoading = false
        }
    }
}
