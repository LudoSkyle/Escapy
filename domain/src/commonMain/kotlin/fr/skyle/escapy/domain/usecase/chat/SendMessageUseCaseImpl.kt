package fr.skyle.escapy.domain.usecase.chat

import fr.skyle.escapy.data.exception.AccessDeniedException
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.chat.api.ChatRepository
import fr.skyle.escapy.data.repository.profile.api.ProfileRepository
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import fr.skyle.escapy.domain.exception.InvalidMessageException
import fr.skyle.escapy.domain.exception.MessageRefusedException
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.validation.ChatRules
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

internal class SendMessageUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val chatRepository: ChatRepository,
) : SendMessageUseCase {

    override suspend fun invoke(gameId: String, text: String) {
        if (!ChatRules.isValidMessage(text)) throw InvalidMessageException()
        withDomainExceptions {
            val userId = sessionRepository.watchSession().filterIsInstance<StoredSession.SignedIn>().first().userId
            // The profile as the rules read it: a name changed meanwhile would be refused
            val profile = profileRepository.watchProfile(userId = userId).filterNotNull().first()
            try {
                chatRepository.sendMessage(
                    gameId = gameId,
                    authorId = userId,
                    username = profile.username,
                    avatarId = profile.avatarId,
                    text = ChatRules.cleanMessage(text),
                )
            } catch (exception: AccessDeniedException) {
                throw MessageRefusedException(cause = exception)
            }
        }
    }
}
