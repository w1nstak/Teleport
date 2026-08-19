import SwiftUI

struct ChannelView: View {
    let channel: Channel
    @State private var posts: [ChannelPost] = []

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ChannelHeaderView(channel: channel)

                ForEach(posts) { post in
                    ChannelPostView(post: post, channelTitle: channel.title)
                }
            }
            .padding(.horizontal)
        }
        .navigationTitle(channel.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu {
                    Button("Без звука", action: {})
                    Button("Поиск", action: {})
                    Button(role: .destructive, action: {}) { Text("Отписаться") }
                } label: {
                    Image(systemName: "ellipsis")
                }
            }
        }
    }
}

struct ChannelHeaderView: View {
    let channel: Channel

    var body: some View {
        VStack(spacing: 8) {
            Circle()
                .fill(TeleportTheme.primaryColor.opacity(0.2))
                .frame(width: 72, height: 72)
                .overlay(
                    Text(String(channel.title.prefix(1)))
                        .font(.title.bold())
                        .foregroundColor(TeleportTheme.primaryColor)
                )
            Text(channel.title)
                .font(.title2.bold())
            if let desc = channel.description {
                Text(desc)
                    .font(.subheadline)
                    .foregroundColor(TeleportTheme.textSecondary)
                    .multilineTextAlignment(.center)
            }
            Text("\(channel.subscriberCount) подписчиков")
                .font(.caption)
                .foregroundColor(TeleportTheme.textSecondary)
        }
        .padding(.vertical)
    }
}

struct ChannelPostView: View {
    let post: ChannelPost
    let channelTitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let text = post.text {
                Text(text)
                    .font(.body)
            }

            HStack {
                Image(systemName: "eye")
                    .font(.caption)
                Text("\(post.viewCount)")
                    .font(.caption)
                Spacer()
                Text(timeString(post.createdAt))
                    .font(.caption)
                    .foregroundColor(TeleportTheme.textSecondary)
            }
            .foregroundColor(TeleportTheme.textSecondary)

            if !post.reactions.isEmpty {
                HStack(spacing: 8) {
                    ForEach(post.reactions.indices, id: \.self) { i in
                        HStack(spacing: 2) {
                            Text(post.reactions[i].emoji)
                            Text("\(post.reactions[i].count)")
                                .font(.caption)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(TeleportTheme.secondaryBackground)
                        .cornerRadius(12)
                    }
                }
            }
        }
        .padding()
        .background(TeleportTheme.secondaryBackground)
        .cornerRadius(TeleportTheme.cornerRadius)
    }

    private func timeString(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
}
