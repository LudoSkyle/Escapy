package fr.skyle.escapy.fake

import fr.skyle.escapy.domain.model.Profile
import fr.skyle.escapy.domain.usecase.auth.SendPasswordResetUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInAsGuestUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInWithEmailUseCase
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCase
import fr.skyle.escapy.domain.usecase.auth.SignUpWithEmailUseCase
import fr.skyle.escapy.domain.usecase.profile.EnsureProfileUseCase
import fr.skyle.escapy.domain.usecase.profile.WatchProfileUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** What every fake action shares: the calls it got, a failure to throw, a gate to hold it. */
open class FakeAction {

    val calls = mutableListOf<String>()

    /** Thrown by the next calls when set. */
    var failure: Exception? = null

    /** When set, a call waits for it: the screen stays in its waiting state meanwhile. */
    var gate: CompletableDeferred<Unit>? = null

    protected suspend fun record(call: String) {
        calls += call
        gate?.await()
        failure?.let { exception -> throw exception }
    }
}

class FakeSignInWithEmailUseCase : FakeAction(), SignInWithEmailUseCase {
    override suspend fun invoke(email: String, password: String) = record(email)
}

class FakeSignUpWithEmailUseCase : FakeAction(), SignUpWithEmailUseCase {
    override suspend fun invoke(email: String, password: String) = record(email)
}

class FakeSignInAsGuestUseCase : FakeAction(), SignInAsGuestUseCase {
    override suspend fun invoke() = record("guest")
}

class FakeSendPasswordResetUseCase : FakeAction(), SendPasswordResetUseCase {
    override suspend fun invoke(email: String) = record(email)
}

class FakeSignOutUseCase : FakeAction(), SignOutUseCase {
    override suspend fun invoke() = record("signOut")
}

class FakeEnsureProfileUseCase : FakeAction(), EnsureProfileUseCase {
    override suspend fun invoke(): Profile {
        record("ensure")
        return Profile(username = "Player_ABC234", avatarId = 7)
    }
}

class FakeWatchProfileUseCase : WatchProfileUseCase {

    val profiles = MutableSharedFlow<Profile?>()

    override fun invoke(userId: String): Flow<Profile?> = profiles
}
