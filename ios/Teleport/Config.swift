import Foundation

enum AppConfig {
    /// Публичный сервер из сборки (Release) или localhost (Debug).
    static var apiBaseURL: String {
        if let plist = Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String,
           !plist.isEmpty,
           !plist.contains("$(") {
            return plist.hasSuffix("/") ? plist : plist + "/"
        }
        #if DEBUG
        return "http://127.0.0.1:8765/"
        #else
        return "https://teleport-w1nst.amvera.io/"
        #endif
    }

    static func wsURL(token: String) -> URL? {
        var base = apiBaseURL
        if base.hasPrefix("https://") {
            base = "wss://" + base.dropFirst("https://".count)
        } else if base.hasPrefix("http://") {
            base = "ws://" + base.dropFirst("http://".count)
        }
        let encoded = token.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? token
        return URL(string: base + "ws?token=\(encoded)")
    }
}
