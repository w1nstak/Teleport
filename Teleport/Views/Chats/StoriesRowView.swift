import SwiftUI

struct StoriesRowView: View {
    let stories: [StoryCircle]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                AddStoryButton()

                ForEach(stories) { story in
                    StoryAvatarView(story: story)
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
    }
}

struct AddStoryButton: View {
    var body: some View {
        VStack(spacing: 4) {
            Circle()
                .strokeBorder(Color.gray.opacity(0.3), lineWidth: 2)
                .frame(width: 56, height: 56)
                .overlay(
                    Image(systemName: "plus")
                        .font(.title3)
                        .foregroundColor(TeleportTheme.primaryColor)
                )
            Text("Моя")
                .font(.caption2)
                .foregroundColor(TeleportTheme.textSecondary)
        }
    }
}

struct StoryAvatarView: View {
    let story: StoryCircle

    var body: some View {
        VStack(spacing: 4) {
            Circle()
                .strokeBorder(
                    story.hasUnseenStory ? TeleportTheme.primaryColor : Color.gray.opacity(0.3),
                    lineWidth: 2.5
                )
                .frame(width: 56, height: 56)
                .overlay(
                    Circle()
                        .fill(TeleportTheme.primaryColor.opacity(0.15))
                        .padding(3)
                        .overlay(
                            Text(String(story.userName.prefix(1)))
                                .font(.headline)
                                .foregroundColor(TeleportTheme.primaryColor)
                        )
                )
            Text(story.userName)
                .font(.caption2)
                .foregroundColor(TeleportTheme.textPrimary)
                .lineLimit(1)
        }
        .frame(width: 64)
    }
}
