import Shared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        // Firebase first, when its file is in the app; then Koin, which reads whether it started
        FirebaseStarterKt.startFirebase()
        KoinInitializerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                // The answer of the sign-in of Google, when it comes back through the URL scheme
                .onOpenURL { url in
                    _ = GoogleSignInLauncher_iosKt.handleGoogleSignInUrl(url: url)
                }
        }
    }
}
