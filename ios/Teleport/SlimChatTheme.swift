import SwiftUI

enum SlimColors {
    static let screenBg = Color.black
    static let card = Color(red: 0.11, green: 0.11, blue: 0.12)
    static let textPrimary = Color.white
    static let textSecondary = Color(white: 0.55)
    static let divider = Color(white: 0.2)
    static let accentBlue = Color(red: 0, green: 0.48, blue: 1)
    static let accentGreen = Color(red: 0.2, green: 0.78, blue: 0.35)
    static let welcomeBg = Color.white
    static let welcomeText = Color.black
    static let welcomeSecondary = Color(white: 0.45)
    static let pillGray = Color(white: 0.92)
}

struct SlimGroupCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(spacing: 0) {
            content
        }
        .background(SlimColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}

struct SettingsIconBox: View {
    let systemName: String
    let color: Color

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .fill(color)
                .frame(width: 30, height: 30)
            Image(systemName: systemName)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(.white)
        }
    }
}

struct FloatingBottomNav: View {
    @Binding var selected: MainTab
    let labels: (String) -> String
    let onSelect: (MainTab) -> Void

    var body: some View {
        HStack {
            ForEach(MainTab.allCases, id: \.self) { tab in
                Button {
                    selected = tab
                    onSelect(tab)
                } label: {
                    VStack(spacing: 4) {
                        Image(systemName: tab.icon)
                            .font(.system(size: 20))
                        Text(tabLabel(tab))
                            .font(.system(size: 10))
                    }
                    .frame(maxWidth: .infinity)
                    .foregroundStyle(selected == tab ? SlimColors.textPrimary : SlimColors.textSecondary)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 10)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .padding(.horizontal, 12)
        .padding(.bottom, 8)
    }

    private func tabLabel(_ tab: MainTab) -> String {
        switch tab {
        case .chats: return labels("nav_chats")
        case .contacts: return labels("nav_contacts")
        case .profile: return labels("nav_profile")
        case .settings: return labels("nav_settings")
        case .calls: return labels("nav_calls")
        }
    }
}

struct AvatarView: View {
    let name: String
    var size: CGFloat = 44

    var body: some View {
        ZStack {
            Circle().fill(SlimColors.accentBlue.opacity(0.35))
            Text(initials(name))
                .font(.system(size: size * 0.32, weight: .semibold))
                .foregroundStyle(SlimColors.textPrimary)
        }
        .frame(width: size, height: size)
    }
}
