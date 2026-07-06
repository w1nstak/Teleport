import Foundation

@MainActor
final class WebSocketClient: NSObject, URLSessionWebSocketDelegate {
    private var task: URLSessionWebSocketTask?
    private var token: String?
    var onMessage: ((MessageDto) -> Void)?

    func connect(token: String) {
        self.token = token
        disconnect()
        guard let url = AppConfig.wsURL(token: token) else { return }
        let session = URLSession(configuration: .default, delegate: self, delegateQueue: nil)
        task = session.webSocketTask(with: url)
        task?.resume()
        listen()
    }

    func disconnect() {
        task?.cancel(with: .goingAway, reason: nil)
        task = nil
    }

    private func listen() {
        task?.receive { [weak self] result in
            guard let self else { return }
            switch result {
            case .success(let msg):
                if case .string(let text) = msg {
                    Task { @MainActor in self.handle(text) }
                }
                self.listen()
            case .failure:
                Task { @MainActor in
                    try? await Task.sleep(nanoseconds: 3_000_000_000)
                    if let token = self.token { self.connect(token: token) }
                }
            }
        }
    }

    private func handle(_ text: String) {
        guard let data = text.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              json["event"] as? String == "message",
              let payload = json["payload"],
              let payloadData = try? JSONSerialization.data(withJSONObject: payload),
              let msg = try? JSONDecoder().decode(MessageDto.self, from: payloadData) else { return }
        onMessage?(msg)
    }
}
