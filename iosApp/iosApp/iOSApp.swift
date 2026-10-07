import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Swift reserva init* para constructores: Kotlin/Native expone initKoinIos() como doInitKoinIos().
        KoinInitKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}