package fr.skyle.escapy.fake

import fr.skyle.escapy.domain.model.Profile
import fr.skyle.escapy.domain.model.Reauthentication
import fr.skyle.escapy.domain.usecase.account.ChangeEmailUseCase
import fr.skyle.escapy.domain.usecase.account.ChangePasswordUseCase
import fr.skyle.escapy.domain.usecase.account.DeleteAccountUseCase
import fr.skyle.escapy.domain.usecase.account.LinkGuestAccountUseCase
import fr.skyle.escapy.domain.usecase.account.LinkGuestWithGoogleUseCase
import fr.skyle.escapy.domain.usecase.auth.SendPasswordResetUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInAsGuestUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInWithEmailUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInWithGoogleUseCase
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCase
import fr.skyle.escapy.domain.usecase.auth.SignUpWithEmailUseCase
import fr.skyle.escapy.domain.usecase.profile.EnsureProfileUseCase
import fr.skyle.escapy.domain.usecase.profile.UpdateAvatarUseCase
import fr.skyle.escapy.domain.usecase.profile.UpdateUsernameUseCase
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

class FakeSignInWithGoogleUseCase : FakeAction(), SignInWithGoogleUseCase {
    override suspend fun invoke(idToken: String) = record(idToken)
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

class FakeUpdateUsernameUseCase : FakeAction(), UpdateUsernameUseCase {
    override suspend fun invoke(username: String): Profile {
        record(username)
        return Profile(username = username, avatarId = 7)
    }
}

class FakeUpdateAvatarUseCase : FakeAction(), UpdateAvatarUseCase {
    override suspend fun invoke(avatarId: Int): Profile {
        record(avatarId.toString())
        return Profile(username = "Player_ABC234", avatarId = avatarId)
    }
}

class FakeLinkGuestAccountUseCase : FakeAction(), LinkGuestAccountUseCase {
    override suspend fun invoke(email: String, password: String) = record(email)
}

class FakeLinkGuestWithGoogleUseCase : FakeAction(), LinkGuestWithGoogleUseCase {
    override suspend fun invoke(idToken: String) = record(idToken)
}

class FakeChangeEmailUseCase : FakeAction(), ChangeEmailUseCase {
    override suspend fun invoke(currentPassword: String, newEmail: String) = record(newEmail)
}

class FakeChangePasswordUseCase : FakeAction(), ChangePasswordUseCase {
    override suspend fun invoke(currentPassword: String, newPassword: String) = record("change")
}

class FakeDeleteAccountUseCase : FakeAction(), DeleteAccountUseCase {
    override suspend fun invoke(reauthentication: Reauthentication) =
        record(
            when (reauthentication) {
                Reauthentication.None -> "guest"
                is Reauthentication.WithPassword -> reauthentication.password
                is Reauthentication.WithGoogle -> "google:${reauthentication.idToken}"
            },
        )
}
