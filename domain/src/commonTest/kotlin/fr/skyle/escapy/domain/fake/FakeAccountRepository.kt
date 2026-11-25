package fr.skyle.escapy.domain.fake

import fr.skyle.escapy.data.repository.account.api.AccountRepository

class FakeAccountRepository(
    // Shares the calls of the auth repository, to check their order
    private val calls: MutableList<String>,
) : AccountRepository {

    var failure: Exception? = null

    override suspend fun deleteAccount() {
        calls += "deleteAccount"
        failure?.let { exception -> throw exception }
    }
}
