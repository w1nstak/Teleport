import SwiftUI

struct WelcomeAuthView: View {
    @State private var showUsername = false
    @State private var showRegister = false
    @State private var showPhone = false

    var body: some View {
        NavigationStack {
            ZStack {
                SlimColors.welcomeBg.ignoresSafeArea()
                Circle().fill(SlimColors.pillGray).frame(width: 120).offset(x: -140, y: -200)
                Circle().fill(SlimColors.pillGray).frame(width: 80).offset(x: 150, y: -120)
                Circle().fill(SlimColors.pillGray).frame(width: 60).offset(x: -100, y: 180)

                VStack(spacing: 0) {
                    Spacer()
                    Image(systemName: "bubble.left.fill")
                        .font(.system(size: 56))
                        .foregroundStyle(SlimColors.welcomeText)
                        .padding(.bottom, 20)
                    Text("Простой, Быстрый мессенджер")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundStyle(SlimColors.welcomeText)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    Spacer()
                    VStack(spacing: 12) {
                        Button { showPhone = true } label: {
                            Text("Войти / Регистрация по номеру")
                                .font(.system(size: 16, weight: .semibold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(SlimColors.welcomeText)
                                .foregroundStyle(.white)
                                .clipShape(Capsule())
                        }
                        Button { showUsername = true } label: {
                            Text("Войти по юзернейму")
                                .font(.system(size: 16, weight: .medium))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(SlimColors.pillGray)
                                .foregroundStyle(SlimColors.welcomeSecondary)
                                .clipShape(Capsule())
                        }
                        Button { showRegister = true } label: {
                            Text("Зарегистрироваться без номера")
                                .font(.system(size: 16, weight: .medium))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(SlimColors.pillGray)
                                .foregroundStyle(SlimColors.welcomeSecondary)
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 40)
                }
            }
            .navigationDestination(isPresented: $showUsername) { UsernameLoginView() }
            .navigationDestination(isPresented: $showRegister) { RegisterView() }
            .navigationDestination(isPresented: $showPhone) { PhoneAuthView() }
        }
    }
}

struct UsernameLoginView: View {
    @EnvironmentObject var vm: AppViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var username = ""
    @State private var password = ""
    @State private var showPassword = false

    var body: some View {
        AuthFormScaffold(title: "Вход", subtitle: "Введите @username и пароль", onBack: { dismiss() }) {
            AuthTextField(placeholder: "@username", text: $username)
            AuthSecureField(password: $password, show: $showPassword)
            if let err = vm.errorMessage {
                Text(err).font(.caption).foregroundStyle(.red)
            }
            Spacer()
            AuthPrimaryButton(title: "Войти", enabled: username.count >= 3 && password.count >= 8, loading: vm.isLoading) {
                Task { await vm.login(username: username, password: password) }
            }
        }
    }
}

struct RegisterView: View {
    @EnvironmentObject var vm: AppViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var username = ""
    @State private var password = ""

    var body: some View {
        AuthFormScaffold(title: "Регистрация", subtitle: "Без номера телефона", onBack: { dismiss() }) {
            AuthTextField(placeholder: "Имя", text: $name)
            AuthTextField(placeholder: "@username", text: $username)
            AuthTextField(placeholder: "Пароль (от 8 символов)", text: $password, secure: true)
            if let err = vm.errorMessage {
                Text(err).font(.caption).foregroundStyle(.red)
            }
            Spacer()
            AuthPrimaryButton(
                title: "Создать аккаунт",
                enabled: !name.isEmpty && username.count >= 3 && password.count >= 8,
                loading: vm.isLoading
            ) {
                Task { await vm.register(displayName: name, username: username, password: password) }
            }
        }
    }
}

struct PhoneAuthView: View {
    @EnvironmentObject var vm: AppViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var phone = ""
    @State private var step = 0
    @State private var devCode: String?

    var body: some View {
        AuthFormScaffold(title: "По номеру", subtitle: step == 0 ? "Введите номер телефона" : "Код из SMS", onBack: { dismiss() }) {
            if step == 0 {
                AuthTextField(placeholder: "+7 900 000 00 00", text: $phone)
                if let devCode {
                    Text("Код (dev): \(devCode)").font(.caption).foregroundStyle(SlimColors.welcomeSecondary)
                }
                Spacer()
                AuthPrimaryButton(title: "Получить код", enabled: phone.count >= 10, loading: vm.isLoading) {
                    Task {
                        devCode = await vm.sendSms(phone: phone)
                        if devCode != nil { step = 1 }
                    }
                }
            } else {
                Text("Пока SMS не настроен — войдите по @username")
                    .font(.caption)
                    .foregroundStyle(SlimColors.welcomeSecondary)
                Spacer()
                AuthPrimaryButton(title: "Назад к username", enabled: true, loading: false) { dismiss() }
            }
        }
    }
}

private struct AuthTextField: View {
    let placeholder: String
    @Binding var text: String
    var secure = false

    var body: some View {
        Group {
            if secure { SecureField(placeholder, text: $text) }
            else {
                TextField(placeholder, text: $text)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
            }
        }
        .padding()
        .background(SlimColors.pillGray)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct AuthSecureField: View {
    @Binding var password: String
    @Binding var show: Bool

    var body: some View {
        HStack {
            Group {
                if show { TextField("Пароль", text: $password) }
                else { SecureField("Пароль", text: $password) }
            }
            Button(show ? "Скрыть" : "Показать") { show.toggle() }
                .font(.caption)
        }
        .padding()
        .background(SlimColors.pillGray)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct AuthFormScaffold<Content: View>: View {
    let title: String
    let subtitle: String
    let onBack: () -> Void
    @ViewBuilder let content: Content

    var body: some View {
        ZStack {
            SlimColors.welcomeBg.ignoresSafeArea()
            VStack(alignment: .leading, spacing: 16) {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(SlimColors.welcomeText)
                }
                Text(title).font(.system(size: 28, weight: .bold))
                Text(subtitle).font(.subheadline).foregroundStyle(SlimColors.welcomeSecondary)
                content
            }
            .padding(24)
            .foregroundStyle(SlimColors.welcomeText)
        }
        .navigationBarBackButtonHidden()
    }
}

private struct AuthPrimaryButton: View {
    let title: String
    let enabled: Bool
    let loading: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Group {
                if loading { ProgressView().tint(.white) }
                else { Text(title).fontWeight(.semibold) }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(enabled ? SlimColors.welcomeText : SlimColors.welcomeText.opacity(0.35))
            .foregroundStyle(.white)
            .clipShape(Capsule())
        }
        .disabled(!enabled || loading)
    }
}
