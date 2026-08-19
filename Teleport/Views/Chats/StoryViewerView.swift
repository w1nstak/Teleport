import SwiftUI

struct StoryViewerView: View {
    let stories: [Story]
    @State private var currentIndex = 0
    @Binding var isPresented: Bool
    @State private var progress: CGFloat = 0

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            VStack {
                HStack(spacing: 4) {
                    ForEach(0..<stories.count, id: \.self) { i in
                        GeometryReader { geo in
                            Rectangle()
                                .fill(i < currentIndex ? Color.white : (i == currentIndex ? Color.white.opacity(0.8) : Color.white.opacity(0.3)))
                                .cornerRadius(2)
                        }
                        .frame(height: 3)
                    }
                }
                .padding(.horizontal)
                .padding(.top, 8)

                HStack {
                    Circle()
                        .fill(TeleportTheme.primaryColor.opacity(0.3))
                        .frame(width: 36, height: 36)
                    Text("User")
                        .font(.subheadline.bold())
                        .foregroundColor(.white)
                    Spacer()
                    Button(action: { isPresented = false }) {
                        Image(systemName: "xmark")
                            .foregroundColor(.white)
                            .font(.title3)
                    }
                }
                .padding(.horizontal)

                Spacer()

                if let caption = stories[safe: currentIndex]?.caption {
                    Text(caption)
                        .foregroundColor(.white)
                        .padding()
                }

                HStack {
                    TextField("Ответить...", text: .constant(""))
                        .padding(10)
                        .background(Color.white.opacity(0.2))
                        .cornerRadius(20)
                        .foregroundColor(.white)

                    Button(action: {}) {
                        Image(systemName: "heart")
                            .foregroundColor(.white)
                            .font(.title2)
                    }
                }
                .padding()
            }
        }
        .onTapGesture { location in
            let screenWidth = UIScreen.main.bounds.width
            if location.x < screenWidth / 2 {
                currentIndex = max(0, currentIndex - 1)
            } else {
                if currentIndex < stories.count - 1 {
                    currentIndex += 1
                } else {
                    isPresented = false
                }
            }
        }
    }
}

extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
