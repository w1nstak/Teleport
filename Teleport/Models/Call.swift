import Foundation

struct Call: Identifiable, Codable {
    let id: String
    var callerId: String
    var receiverId: String
    var type: CallType
    var status: CallStatus
    var startedAt: Date?
    var endedAt: Date?
    var duration: TimeInterval?

    enum CallType: String, Codable {
        case voice, video
    }

    enum CallStatus: String, Codable {
        case ringing, active, ended, missed, declined
    }
}
