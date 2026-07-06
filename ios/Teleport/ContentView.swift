import SwiftUI

struct ContentView: View {
    @EnvironmentObject var vm: AppViewModel
    @State private var didRestore = false

    var body: some View {
        Group {
            if vm.token != nil, vm.user != nil {
                MainTabView()
            } else {
                WelcomeAuthView()
            }
        }
        .task {
            guard !didRestore else { return }
            didRestore = true
            await vm.restoreSession()
        }
    }
}
