package fr.skyle.escapy.ui.core.error

import androidx.compose.runtime.Composable
import fr.skyle.escapy.domain.exception.ExcludedFromGameException
import fr.skyle.escapy.domain.exception.GameFullException
import fr.skyle.escapy.domain.exception.GameNotFoundException
import fr.skyle.escapy.domain.exception.GameNotJoinableException
import fr.skyle.escapy.domain.exception.InvalidGameTitleException
import fr.skyle.escapy.domain.exception.InvalidJoinCodeException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.exception.TooManyAttemptsException
import fr.skyle.escapy.domain.exception.TooManyGamesException
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.error_excluded_from_game
import fr.skyle.escapy.resources.error_game_full
import fr.skyle.escapy.resources.error_game_not_found
import fr.skyle.escapy.resources.error_game_not_joinable
import fr.skyle.escapy.resources.error_game_title_invalid
import fr.skyle.escapy.resources.error_join_code_invalid
import fr.skyle.escapy.resources.error_network
import fr.skyle.escapy.resources.error_not_a_game_qr_code
import fr.skyle.escapy.resources.error_too_many_attempts
import fr.skyle.escapy.resources.error_too_many_games
import fr.skyle.escapy.resources.error_unknown
import org.jetbrains.compose.resources.stringResource

/** What went wrong with a game, as a screen tells it. */
enum class GameError {
    INVALID_GAME_TITLE,
    TOO_MANY_GAMES,
    INVALID_JOIN_CODE,
    GAME_NOT_FOUND,
    GAME_FULL,
    GAME_NOT_JOINABLE,
    EXCLUDED_FROM_GAME,
    TOO_MANY_ATTEMPTS,
    // A QR code read by the camera that holds no code of a game: never sent to the server
    NOT_A_GAME_QR_CODE,
    NETWORK,
    UNKNOWN;

    /** Whether the error is about the code typed, and shown under its field rather than in a message. */
    val isAboutJoinCode: Boolean
        get() = this in setOf(INVALID_JOIN_CODE, GAME_NOT_FOUND, GAME_FULL, GAME_NOT_JOINABLE, EXCLUDED_FROM_GAME, TOO_MANY_ATTEMPTS)
}

internal fun Throwable.toGameError(): GameError =
    when (this) {
        is InvalidGameTitleException -> GameError.INVALID_GAME_TITLE
        is TooManyGamesException -> GameError.TOO_MANY_GAMES
        is InvalidJoinCodeException -> GameError.INVALID_JOIN_CODE
        is GameNotFoundException -> GameError.GAME_NOT_FOUND
        is GameFullException -> GameError.GAME_FULL
        is GameNotJoinableException -> GameError.GAME_NOT_JOINABLE
        is ExcludedFromGameException -> GameError.EXCLUDED_FROM_GAME
        is TooManyAttemptsException -> GameError.TOO_MANY_ATTEMPTS
        is NetworkUnavailableException -> GameError.NETWORK
        else -> GameError.UNKNOWN
    }

@Composable
internal fun GameError.message(): String =
    stringResource(
        when (this) {
            GameError.INVALID_GAME_TITLE -> Res.string.error_game_title_invalid
            GameError.TOO_MANY_GAMES -> Res.string.error_too_many_games
            GameError.INVALID_JOIN_CODE -> Res.string.error_join_code_invalid
            GameError.GAME_NOT_FOUND -> Res.string.error_game_not_found
            GameError.GAME_FULL -> Res.string.error_game_full
            GameError.GAME_NOT_JOINABLE -> Res.string.error_game_not_joinable
            GameError.EXCLUDED_FROM_GAME -> Res.string.error_excluded_from_game
            GameError.TOO_MANY_ATTEMPTS -> Res.string.error_too_many_attempts
            GameError.NOT_A_GAME_QR_CODE -> Res.string.error_not_a_game_qr_code
            GameError.NETWORK -> Res.string.error_network
            GameError.UNKNOWN -> Res.string.error_unknown
        },
    )

/** The message of each error, read in the composition, for a snackbar shown from a coroutine. */
@Composable
internal fun gameErrorMessages(): Map<GameError, String> =
    GameError.entries.associateWith { error -> error.message() }
