import SwiftUI

struct MainTabView: View {
    @EnvironmentObject var vm: AppViewModel
    @State private var tab: MainTab = .chats
    @State private var chatPath = NavigationPath()

    var body: some View {
        ZStack(alignment: .bottom) {
            SlimColors.screenBg.ignoresSafeArea()
            Group {
                switch tab {
                case .chats:
                    NavigationStack(path: $chatPath) {
                        ChatListView(path: $chatPath)
                            .navigationDestination(for: String.self) { chatId in
                                ChatDetailView(chatId: chatId)
                            }
                    }
                case .contacts:
                    NavigationStack { ContactsTabView() }
                case .profile:
                    NavigationStack { ProfileTabView() }
                case .settings:
                    NavigationStack { SettingsTabView() }
                case .calls:
                    NavigationStack { CallsTabView() }
                }
            }
            .padding(.bottom, 72)
            FloatingBottomNav(selected: $tab, labels: vm.label, onSelect: { _ in })
        }
    }
}

struct ChatListView: View {
    @EnvironmentObject var vm: AppViewModel
    @Binding var path: NavigationPath
    @State private var search = ""
    @State private var showMenu = false
    @State private var selectionMode = false
    @State private var selected = Set<String>()

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text(vm.label("chats_title"))
                    .font(.system(size: 34, weight: .bold))
                Spacer()
                Button { showMenu = true } label: {
                    Image(systemName: "ellipsis.circle")
                        .font(.title2)
                        .foregroundStyle(SlimColors.textPrimary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)

            HStack {
                Image(systemName: "magnifyingglass").foregroundStyle(SlimColors.textSecondary)
                TextField(vm.label("search"), text: $search)
                    .textInputAutocapitalization(.never)
                    .onChange(of: search) { _, q in
                        Task { await vm.searchUsers(q) }
                    }
            }
            .padding(10)
            .background(SlimColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(.horizontal, 16)
            .padding(.vertical, 8)

            if !search.isEmpty, !vm.searchResults.isEmpty {
                searchResultsList
            } else {
                chatList
            }
        }
        .background(SlimColors.screenBg)
        .confirmationDialog("Меню", isPresented: $showMenu) {
            Button("Поиск") { }
            Button("Архив") { }
            Button("Прочитать все") { vm.markAllRead() }
            Button("Выбрать чаты") { selectionMode.toggle() }
            Button("Отмена", role: .cancel) { }
        }
        .task { await vm.reloadChats() }
    }

    private var chatList: some View {
        List {
            if vm.displayChats.isEmpty {
                Text("Нет чатов. Найдите пользователя в поиске.")
                    .foregroundStyle(SlimColors.textSecondary)
                    .listRowBackground(Color.clear)
            }
            ForEach(vm.displayChats) { chat in
                Button {
                    if selectionMode {
                        if selected.contains(chat.chatId) { selected.remove(chat.chatId) }
                        else { selected.insert(chat.chatId) }
                    } else {
                        path.append(chat.chatId)
                        Task { await vm.openChat(chat) }
                    }
                } label: {
                    HStack(spacing: 12) {
                        if selectionMode {
                            Image(systemName: selected.contains(chat.chatId) ? "checkmark.circle.fill" : "circle")
                                .foregroundStyle(SlimColors.accentBlue)
                        }
                        AvatarView(name: chat.title, size: 48)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(chat.title).font(.headline).foregroundStyle(SlimColors.textPrimary)
                            Text(chat.type == "PRIVATE" ? "личный чат" : chat.type)
                                .font(.caption)
                                .foregroundStyle(SlimColors.textSecondary)
                        }
                    }
                }
                .listRowBackground(SlimColors.screenBg)
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
    }

    private var searchResultsList: some View {
        List(vm.searchResults) { user in
            Button {
                Task {
                    await vm.openChatWithUser(user)
                    path.append(vm.currentChatId ?? "")
                    search = ""
                }
            } label: {
                HStack {
                    AvatarView(name: user.displayName)
                    VStack(alignment: .leading) {
                        Text(user.displayName).foregroundStyle(SlimColors.textPrimary)
                        if let u = user.username {
                            Text("@\(u)").font(.caption).foregroundStyle(SlimColors.textSecondary)
                        }
                    }
                }
            }
            .listRowBackground(SlimColors.screenBg)
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
    }
}

struct ChatDetailView: View {
    @EnvironmentObject var vm: AppViewModel
    let chatId: String
    @State private var draft = ""
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button { dismiss() } label: {
                    Image(systemName: "chevron.left").foregroundStyle(SlimColors.textPrimary)
                }
                AvatarView(name: vm.currentChatTitle, size: 36)
                VStack(alignment: .leading) {
                    Text(vm.currentChatTitle).font(.headline).foregroundStyle(SlimColors.textPrimary)
                    Text(vm.currentPeer?.username.map { "@\($0)" } ?? "личный чат")
                        .font(.caption).foregroundStyle(SlimColors.textSecondary)
                }
                Spacer()
            }
            .padding()
            .background(SlimColors.card)

            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 8) {
                        ForEach(vm.messages.filter { $0.chatId == chatId }) { msg in
                            MessageBubble(message: msg, isMine: msg.senderId == vm.user?.id)
                                .id(msg.id)
                        }
                    }
                    .padding()
                }
                .onChange(of: vm.messages.count) { _, _ in
                    if let last = vm.messages.last { proxy.scrollTo(last.id, anchor: .bottom) }
                }
            }

            HStack {
                TextField(vm.label("message_placeholder"), text: $draft)
                    .padding(10)
                    .background(SlimColors.card)
                    .clipShape(Capsule())
                Button {
                    let text = draft
                    draft = ""
                    Task { await vm.sendMessage(text) }
                } label: {
                    Image(systemName: "paperplane.fill")
                        .foregroundStyle(SlimColors.accentBlue)
                }
            }
            .padding()
        }
        .background(SlimColors.screenBg)
        .navigationBarBackButtonHidden()
        .task { await vm.loadMessages() }
    }
}

struct MessageBubble: View {
    let message: MessageDto
    let isMine: Bool

    var body: some View {
        HStack {
            if isMine { Spacer(minLength: 48) }
            VStack(alignment: isMine ? .trailing : .leading, spacing: 4) {
                Text(message.text)
                    .foregroundStyle(isMine ? .white : SlimColors.textPrimary)
                Text(formatTime(message.createdAt))
                    .font(.caption2)
                    .foregroundStyle(isMine ? .white.opacity(0.7) : SlimColors.textSecondary)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(isMine ? SlimColors.accentBlue : SlimColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            if !isMine { Spacer(minLength: 48) }
        }
    }
}

struct ContactsTabView: View {
    @EnvironmentObject var vm: AppViewModel
    @State private var query = ""

    var filtered: [ChatListItem] {
        let list = vm.chats.filter { $0.type == "PRIVATE" }
        guard !query.isEmpty else { return list }
        return list.filter { $0.title.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Контакты").font(.system(size: 34, weight: .bold)).padding(16)
            TextField("Поиск", text: $query)
                .padding(10)
                .background(SlimColors.card)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal, 16)
            List(filtered) { chat in
                HStack {
                    AvatarView(name: chat.title)
                    Text(chat.title).foregroundStyle(SlimColors.textPrimary)
                }
                .listRowBackground(SlimColors.screenBg)
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
        }
        .background(SlimColors.screenBg)
        .task { await vm.reloadChats() }
    }
}

struct CallsTabView: View {
    var body: some View {
        VStack(alignment: .leading) {
            Text("Звонки").font(.system(size: 34, weight: .bold)).padding(16)
            Text("История звонков появится здесь")
                .foregroundStyle(SlimColors.textSecondary)
                .padding(.horizontal, 16)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(SlimColors.screenBg)
    }
}
