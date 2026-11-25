package fr.skyle.escapy.domain.usecase.auth

/** Signs in as a guest, without an email. */
interface SignInAsGuestUseCase {
    suspend operator fun invoke()
}
