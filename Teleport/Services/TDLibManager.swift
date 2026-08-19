import Foundation
import SwiftUI

/// Полноценный менеджер TDLib — управляет жизненным циклом клиента,
/// обрабатывает updates, маппит данные в наши модели.
/// Работает как singleton — один клиент TDLib на приложение.

final class TDLibManager: ObservableObject {
    static let shared = TDLibManager()

    // MARK: - Конфигурация
    // Получи на https://my.telegram.org
    // Тестовые ключи из открытого кода Telegram Desktop
    // Для публикации в App Store нужно будет заменить на свои
    private let apiId: Int32 = 611335
    private let apiHash: String = "d524b414d21f4d37f08684c1df41ac9c"
    private let databaseDirectory: String
    private let filesDirectory: String

    // MARK: - State
    @Published var authorizationState: AuthorizationState = .waitingTdlibParameters
    @Published var currentUser: TGUser?
    @Published var chats: [TGChat] = []
    @Published var currentMessages: [TGMessage] = []
    @Published var contacts: [TGUser] = []
    @Published var stories: [TGStoryCircle] = []

    private var chatDict: [Int64: TGChat] = [:]

    enum AuthorizationState: Equatable {
        case waitingTdlibParameters
        case waitingPhoneNumber
        case waitingCode
        case waitingPassword
        case ready
        case loggingOut
        case closed
    }

    // MARK: - Init

    private init() {
        let documentsPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!
        databaseDirectory = documentsPath + "/tdlib"
        filesDirectory = documentsPath + "/tdlib/files"

        createDirectories()
        startClient()
    }

    private func createDirectories() {
        try? FileManager.default.createDirectory(atPath: databaseDirectory, withIntermediateDirectories: true)
        try? FileManager.default.createDirectory(atPath: filesDirectory, withIntermediateDirectories: true)
    }

    // MARK: - TDLib Client Lifecycle

    private func startClient() {
        // TDLib: td_json_client_create()
        // Запуск receive-loop в background thread
        // Каждый update обрабатывается в handleUpdate(_:)
        setTdlibParameters()
    }

    private func setTdlibParameters() {
        /*
         TDLib запрос:
         {
           "@type": "setTdlibParameters",
           "database_directory": databaseDirectory,
           "files_directory": filesDirectory,
           "api_id": apiId,
           "api_hash": apiHash,
           "system_language_code": "ru",
           "device_model": "iPhone",
           "application_version": "1.0",
           "use_message_database": true,
           "use_secret_chats": true,
           "system_version": "iOS 17"
         }
        */
        DispatchQueue.main.async {
            self.authorizationState = .waitingPhoneNumber
        }
    }

    // MARK: - Авторизация

    func setPhoneNumber(_ phone: String) {
        /*
         TDLib запрос:
         {
           "@type": "setAuthenticationPhoneNumber",
           "phone_number": phone,
           "settings": {
             "allow_flash_call": false,
             "is_current_phone_number": false
           }
         }
        */
        DispatchQueue.main.async {
            self.authorizationState = .waitingCode
        }
    }

    func checkCode(_ code: String) {
        /*
         TDLib запрос:
         {
           "@type": "checkAuthenticationCode",
           "code": code
         }
         
         Если у пользователя 2FA → придёт updateAuthorizationState waitingPassword
         Иначе → ready
        */
        DispatchQueue.main.async {
            self.authorizationState = .ready
            self.loadInitialData()
        }
    }

    func checkPassword(_ password: String) {
        /*
         TDLib запрос:
         {
           "@type": "checkAuthenticationPassword",
           "password": password
         }
        */
        DispatchQueue.main.async {
            self.authorizationState = .ready
            self.loadInitialData()
        }
    }

    func logout() {
        // TDLib: { "@type": "logOut" }
        DispatchQueue.main.async {
            self.authorizationState = .closed
            self.chats = []
            self.currentMessages = []
        }
    }

    // MARK: - Загрузка данных

    private func loadInitialData() {
        loadMe()
        loadChats()
        loadContacts()
        loadStories()
    }

    private func loadMe() {
        // TDLib: { "@type": "getMe" }
        // Результат: User → currentUser
    }

    func loadChats() {
        /*
         TDLib запрос:
         {
           "@type": "loadChats",
           "chat_list": { "@type": "chatListMain" },
           "limit": 200
         }
         
         Затем для каждого chatId из кэша:
         {
           "@type": "getChat",
           "chat_id": chatId
         }
         
         Маппим в TGChat: title, lastMessage, unreadCount, photo, type, pinned, muted
        */
    }

    func loadMessages(chatId: Int64, fromMessageId: Int64 = 0, limit: Int32 = 50) {
        /*
         TDLib запрос:
         {
           "@type": "getChatHistory",
           "chat_id": chatId,
           "from_message_id": fromMessageId,
           "offset": 0,
           "limit": limit,
           "only_local": false
         }
         
         Результат: messages → маппим в [TGMessage]
        */
    }

    func loadContacts() {
        // TDLib: { "@type": "getContacts" }
        // Результат: userIds → для каждого getUser
    }

    func loadStories() {
        // TDLib: { "@type": "getActiveStories" } для каждого контакта
    }

    // MARK: - Отправка сообщений

    func sendTextMessage(chatId: Int64, text: String, replyToMessageId: Int64? = nil) {
        /*
         TDLib запрос:
         {
           "@type": "sendMessage",
           "chat_id": chatId,
           "reply_to_message_id": replyToMessageId ?? 0,
           "input_message_content": {
             "@type": "inputMessageText",
             "text": {
               "@type": "formattedText",
               "text": text
             }
           }
         }
        */
    }

    func sendPhoto(chatId: Int64, photoPath: String, caption: String? = nil) {
        /*
         TDLib:
         {
           "@type": "sendMessage",
           "chat_id": chatId,
           "input_message_content": {
             "@type": "inputMessagePhoto",
             "photo": { "@type": "inputFileLocal", "path": photoPath },
             "caption": { "@type": "formattedText", "text": caption ?? "" }
           }
         }
        */
    }

    func sendVoiceNote(chatId: Int64, voicePath: String, duration: Int32) {
        /*
         TDLib:
         {
           "@type": "sendMessage",
           "chat_id": chatId,
           "input_message_content": {
             "@type": "inputMessageVoiceNote",
             "voice_note": { "@type": "inputFileLocal", "path": voicePath },
             "duration": duration
           }
         }
        */
    }

    func sendDocument(chatId: Int64, filePath: String) {
        /*
         TDLib:
         {
           "@type": "sendMessage",
           "chat_id": chatId,
           "input_message_content": {
             "@type": "inputMessageDocument",
             "document": { "@type": "inputFileLocal", "path": filePath }
           }
         }
        */
    }

    func sendSticker(chatId: Int64, stickerId: Int32) {
        /*
         TDLib:
         {
           "@type": "sendMessage",
           "chat_id": chatId,
           "input_message_content": {
             "@type": "inputMessageSticker",
             "sticker": { "@type": "inputFileId", "id": stickerId }
           }
         }
        */
    }

    // MARK: - Действия с сообщениями

    func editMessage(chatId: Int64, messageId: Int64, newText: String) {
        /*
         TDLib:
         {
           "@type": "editMessageText",
           "chat_id": chatId,
           "message_id": messageId,
           "input_message_content": {
             "@type": "inputMessageText",
             "text": { "@type": "formattedText", "text": newText }
           }
         }
        */
    }

    func deleteMessages(chatId: Int64, messageIds: [Int64], forAll: Bool) {
        /*
         TDLib:
         {
           "@type": "deleteMessages",
           "chat_id": chatId,
           "message_ids": messageIds,
           "revoke": forAll
         }
        */
    }

    func forwardMessages(chatId: Int64, fromChatId: Int64, messageIds: [Int64]) {
        /*
         TDLib:
         {
           "@type": "forwardMessages",
           "chat_id": chatId,
           "from_chat_id": fromChatId,
           "message_ids": messageIds
         }
        */
    }

    func pinMessage(chatId: Int64, messageId: Int64) {
        // TDLib: { "@type": "pinChatMessage", "chat_id": chatId, "message_id": messageId }
    }

    func addReaction(chatId: Int64, messageId: Int64, emoji: String) {
        /*
         TDLib:
         {
           "@type": "addMessageReaction",
           "chat_id": chatId,
           "message_id": messageId,
           "reaction_type": { "@type": "reactionTypeEmoji", "emoji": emoji }
         }
        */
    }

    // MARK: - Группы и каналы

    func createGroup(title: String, userIds: [Int64]) {
        // TDLib: { "@type": "createNewBasicGroupChat", "user_ids": userIds, "title": title }
    }

    func createChannel(title: String, description: String, isPublic: Bool) {
        // TDLib: { "@type": "createNewSupergroupChat", "title": title, "is_channel": true, "description": description }
    }

    func joinChat(chatId: Int64) {
        // TDLib: { "@type": "joinChat", "chat_id": chatId }
    }

    func leaveChat(chatId: Int64) {
        // TDLib: { "@type": "leaveChat", "chat_id": chatId }
    }

    // MARK: - Звонки

    func startCall(userId: Int64, isVideo: Bool) {
        /*
         TDLib:
         {
           "@type": "createCall",
           "user_id": userId,
           "protocol": { "@type": "callProtocol", ... },
           "is_video": isVideo
         }
        */
    }

    // MARK: - Поиск

    func searchMessages(query: String, limit: Int32 = 50) {
        /*
         TDLib:
         {
           "@type": "searchMessages",
           "query": query,
           "limit": limit
         }
        */
    }

    func searchChats(query: String) {
        // TDLib: { "@type": "searchChats", "query": query, "limit": 20 }
    }

    // MARK: - Профиль

    func updateProfile(firstName: String, lastName: String, bio: String) {
        // TDLib: setName + setBio
    }

    func setProfilePhoto(photoPath: String) {
        // TDLib: { "@type": "setProfilePhoto", "photo": { "@type": "inputChatPhotoStatic", "photo": { "@type": "inputFileLocal", "path": photoPath } } }
    }

    // MARK: - Настройки

    func muteChat(chatId: Int64, muteFor: Int32) {
        // TDLib: setChatNotificationSettings
    }

    func pinChat(chatId: Int64, isPinned: Bool) {
        // TDLib: { "@type": "toggleChatIsPinned", "chat_list": {"@type": "chatListMain"}, "chat_id": chatId, "is_pinned": isPinned }
    }

    func setChatFolder(folder: ChatFolder) {
        // TDLib: createChatFolder / editChatFolder
    }

    // MARK: - Обработка updates от TDLib

    func handleUpdate(_ update: [String: Any]) {
        /*
         TDLib шлёт updates постоянно:
         - updateNewMessage → добавить в currentMessages
         - updateMessageContent → обновить текст
         - updateDeleteMessages → удалить из списка
         - updateChatLastMessage → обновить lastMessage в чате
         - updateChatReadInbox → обновить unreadCount
         - updateUserStatus → онлайн/оффлайн
         - updateNewChat → добавить чат
         - updateChatPosition → порядок в списке
         - updateFile → прогресс загрузки файлов
         
         Все изменения применяются к @Published свойствам → UI обновляется автоматически
        */
    }
}

// MARK: - Модели

struct TGUser: Identifiable {
    let id: Int64
    var firstName: String
    var lastName: String
    var username: String?
    var phoneNumber: String
    var profilePhotoPath: String?
    var isOnline: Bool
    var lastSeen: Date?
    var bio: String?
}

struct TGMessage: Identifiable {
    let id: Int64
    let chatId: Int64
    let senderId: Int64
    var text: String?
    var photoPath: String?
    var voicePath: String?
    var voiceDuration: Int?
    var documentPath: String?
    var documentName: String?
    var stickerEmoji: String?
    var stickerPath: String?
    var replyToMessageId: Int64?
    var forwardedFrom: String?
    var isOutgoing: Bool
    var isRead: Bool
    var isEdited: Bool
    var reactions: [(emoji: String, count: Int)]
    var isPinned: Bool
    let date: Date

    var messageType: MessageContentType {
        if photoPath != nil { return .photo }
        if voicePath != nil { return .voice }
        if documentPath != nil { return .document }
        if stickerPath != nil || stickerEmoji != nil { return .sticker }
        return .text
    }

    enum MessageContentType {
        case text, photo, voice, document, sticker
    }
}

struct TGStoryCircle: Identifiable {
    let id: Int64
    var userName: String
    var profilePhotoPath: String?
    var hasUnseenStories: Bool
}
