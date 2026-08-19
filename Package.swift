// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "Teleport",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "Teleport", targets: ["Teleport"])
    ],
    dependencies: [
        // TDLibKit — Swift-обёртка над TDLib (официальный Telegram API)
        .package(url: "https://github.com/nicklama/TDLibKit", from: "1.0.0"),
    ],
    targets: [
        .target(
            name: "Teleport",
            dependencies: ["TDLibKit"],
            path: "Teleport"
        )
    ]
)
