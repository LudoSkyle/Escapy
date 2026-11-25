package fr.skyle.escapy.ui.screen.about.ui

import androidx.lifecycle.ViewModel

/** What the app says about itself: its version, given by the host when it starts. */
class AboutViewModel(
    val appVersion: String,
) : ViewModel()
