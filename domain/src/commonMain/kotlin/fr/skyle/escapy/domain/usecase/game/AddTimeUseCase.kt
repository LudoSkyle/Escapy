package fr.skyle.escapy.domain.usecase.game

/**
 * Adds [minutes] to a started game the user runs, which had [addedMinutes] added when the user
 * asked: a call sent again adds nothing twice. Throws InvalidAddedTimeException,
 * GameFinishedException.
 */
interface AddTimeUseCase {
    suspend operator fun invoke(gameId: String, minutes: Int, addedMinutes: Int)
}
