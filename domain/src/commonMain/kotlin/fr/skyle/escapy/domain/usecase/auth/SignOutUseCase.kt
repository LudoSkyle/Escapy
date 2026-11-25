package fr.skyle.escapy.domain.usecase.auth

/** Signs out. A guest loses their account: the screen warns them first. */
interface SignOutUseCase {
    suspend operator fun invoke()
}
