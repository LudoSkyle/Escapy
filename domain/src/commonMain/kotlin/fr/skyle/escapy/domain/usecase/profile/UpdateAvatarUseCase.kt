package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.domain.model.Profile

/** Changes the avatar, one of the avatars of the app. */
interface UpdateAvatarUseCase {
    suspend operator fun invoke(avatarId: Int): Profile
}
