package fr.skyle.escapy.data.repository.account.api

interface AccountRepository {

    /**
     * Deletes the account of the signed-in user and all their data, on the server, then signs out
     * on this device. A member signs in again first (AuthRepository.reauthenticate), or the server
     * refuses with RECENT_SIGN_IN_REQUIRED.
     */
    suspend fun deleteAccount()
}
