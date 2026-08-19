import SwiftUI

struct MainTabView: View {
    var body: some View {
        TabView {
            ChatsListView()
                .tabItem {
                    Image(systemName: "bubble.left.and.bubble.right.fill")
                    Text("Чаты")
                }

            ContactsView()
                .tabItem {
                    Image(systemName: "person.2.fill")
                    Text("Контакты")
                }

            CallsListView()
                .tabItem {
                    Image(systemName: "phone.fill")
                    Text("Звонки")
                }

            SettingsView()
                .tabItem {
                    Image(systemName: "gearshape.fill")
                    Text("Настройки")
                }
        }
        .tint(TeleportTheme.primaryColor)
    }
}
