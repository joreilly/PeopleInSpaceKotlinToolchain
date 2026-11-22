package dev.johnoreilly.peopleinspace

import androidx.compose.ui.window.ComposeUIViewController

fun ViewController() = ComposeUIViewController { App() }

/** Called once from the Swift app's init. */
fun startKoin() {
    initKoin()
}
