import Foundation

enum APIError: LocalizedError {
    case invalidURL
    case http(Int, String)
    case decode(Error)
    case network(Error)

    var errorDescription: String? {
        switch self {
        case .invalidURL: return "Неверный URL сервера"
        case .http(let code, let msg): return msg.isEmpty ? "Ошибка \(code)" : msg
        case .decode: return "Ошибка разбора ответа"
        case .network(let e): return e.localizedDescription
        }
    }
}

final class TeleportAPI {
    static let shared = TeleportAPI()
    private let decoder: JSONDecoder = {
        let d = JSONDecoder()
        return d
    }()
    private let encoder = JSONEncoder()

    private func url(_ path: String) throws -> URL {
        guard let u = URL(string: AppConfig.apiBaseURL + path.trimmingCharacters(in: CharacterSet(charactersIn: "/"))) else {
            throw APIError.invalidURL
        }
        return u
    }

    private func request<T: Decodable>(
        _ path: String,
        method: String = "GET",
        token: String? = nil,
        body: Encodable? = nil
    ) async throws -> T {
        var req = URLRequest(url: try url(path))
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let token { req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization") }
        if let body {
            req.httpBody = try encoder.encode(AnyEncodable(body))
        }
        do {
            let (data, res) = try await URLSession.shared.data(for: req)
            guard let http = res as? HTTPURLResponse else { throw APIError.http(0, "Нет ответа") }
            if http.statusCode >= 400 {
                let msg = (try? JSONDecoder().decode([String: String].self, from: data))?["detail"]
                    ?? String(data: data, encoding: .utf8)
                    ?? ""
                throw APIError.http(http.statusCode, msg)
            }
            if T.self == EmptyResponse.self, data.isEmpty {
                return EmptyResponse() as! T
            }
            do {
                return try decoder.decode(T.self, from: data)
            } catch {
                throw APIError.decode(error)
            }
        } catch let e as APIError {
            throw e
        } catch {
            throw APIError.network(error)
        }
    }

    func login(username: String, password: String) async throws -> AuthResponse {
        try await request("auth/login/username", method: "POST", body: UsernameLoginRequest(username: username, password: password))
    }

    func register(displayName: String, username: String, password: String) async throws -> AuthResponse {
        try await request("auth/register/web", method: "POST", body: WebRegisterRequest(displayName: displayName, username: username, password: password))
    }

    func sendSms(phone: String) async throws -> SmsSendResponse {
        try await request("auth/sms/send", method: "POST", body: SmsSendRequest(phone: phone))
    }

    func verifySms(phone: String, code: String) async throws -> SmsVerifyResponse {
        try await request("auth/sms/verify", method: "POST", body: SmsVerifyRequest(phone: phone, code: code))
    }

    func me(token: String) async throws -> UserDto {
        try await request("users/me", token: token)
    }

    func updateMe(token: String, body: UpdateProfileRequest) async throws -> UserDto {
        try await request("users/me", method: "PATCH", token: token, body: body)
    }

    func searchUsers(token: String, q: String) async throws -> [UserDto] {
        try await request("users/search?q=\(q.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? q)", token: token)
    }

    func listChats(token: String) async throws -> [ChatListItem] {
        try await request("chats", token: token)
    }

    func openChat(token: String, otherUserId: String) async throws -> OpenChatResponse {
        try await request("chats/open", method: "POST", token: token, body: OpenChatRequest(otherUserId: otherUserId))
    }

    func messages(token: String, chatId: String) async throws -> [MessageDto] {
        let page: MessagesPage = try await request("chats/\(chatId)/messages?since=0&limit=300", token: token)
        return page.messages
    }

    func sendMessage(token: String, chatId: String, text: String) async throws -> MessageDto {
        try await request("messages/send", method: "POST", token: token, body: SendMessageRequest(chatId: chatId, type: "TEXT", text: text))
    }
}

struct EmptyResponse: Codable {}

private struct AnyEncodable: Encodable {
    private let encodeFunc: (Encoder) throws -> Void
    init<T: Encodable>(_ wrapped: T) {
        encodeFunc = wrapped.encode
    }
    func encode(to encoder: Encoder) throws { try encodeFunc(encoder) }
}
