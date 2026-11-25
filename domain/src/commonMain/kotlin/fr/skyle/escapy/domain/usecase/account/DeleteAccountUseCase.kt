package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.domain.model.Reauthentication

/**
 * Deletes the account and all its data, then signs out. A member signs in again first, with their
 * password or with Google; a guest, who has nothing to sign in with, gives Reauthentication.None.
 * Also what signing out does for a guest: an account nobody can sign in to again is not left on the
 * server.
 */
interface DeleteAccountUseCase {
    suspend operator fun invoke(reauthentication: Reauthentication)
}
