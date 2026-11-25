package fr.skyle.escapy.ui.screen.about.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import fr.skyle.escapy.ui.core.link.EscapyLinks
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AboutRoute(
    navigateBack: () -> Unit,
    viewModel: AboutViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current

    AboutScreen(
        appVersion = viewModel.appVersion,
        onBackClicked = navigateBack,
        onSourceCodeClicked = { uriHandler.openUri(EscapyLinks.SOURCE_CODE) },
        onPrivacyPolicyClicked = { uriHandler.openUri(EscapyLinks.PRIVACY_POLICY) },
    )
}
