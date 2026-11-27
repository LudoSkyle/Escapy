package fr.skyle.escapy.data.repository.mission

import fr.skyle.escapy.data.document.MasterMissionDocument
import fr.skyle.escapy.data.document.MissionDocument
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.functionCall
import fr.skyle.escapy.data.exception.withFirestoreErrors
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.firebase.FirebaseServices
import fr.skyle.escapy.data.function.MissionRequest
import fr.skyle.escapy.data.function.SaveMissionRequest
import fr.skyle.escapy.data.function.SubmitAnswerRequest
import fr.skyle.escapy.data.function.SubmitAnswerResponse
import fr.skyle.escapy.data.function.TriggerRequest
import fr.skyle.escapy.data.model.StoredMission
import fr.skyle.escapy.data.repository.mission.api.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class MissionRepositoryImpl(
    private val firebaseProvider: FirebaseProvider,
) : MissionRepository {

    private fun services(): FirebaseServices =
        firebaseProvider.services() ?: throw FirebaseUnavailableException()

    private fun game(gameId: String) = services().firestore.collection(GAMES).document(gameId)

    override fun watchMissions(gameId: String): Flow<List<StoredMission>> =
        game(gameId).collection(MISSIONS).orderBy(CREATED_AT).snapshots
            .map { snapshot ->
                snapshot.documents.map { document ->
                    val mission = document.data<MissionDocument>()
                    StoredMission(
                        id = document.id,
                        title = mission.title,
                        statement = mission.statement,
                        type = mission.type,
                        codeLength = mission.codeLength,
                        isFinal = mission.isFinal,
                        state = mission.state,
                        solverId = mission.solvedBy?.userId,
                        solverUsername = mission.solvedBy?.username,
                        answers = emptyList(),
                        triggerType = null,
                        triggerMissionId = null,
                    )
                }
            }
            .distinctUntilChanged()
            .withFirestoreErrors()

    override fun watchMasterMissions(gameId: String): Flow<List<StoredMission>> =
        game(gameId).collection(MASTER_MISSIONS).orderBy(CREATED_AT).snapshots
            .map { snapshot ->
                snapshot.documents.map { document ->
                    val mission = document.data<MasterMissionDocument>()
                    StoredMission(
                        id = document.id,
                        title = mission.title,
                        statement = mission.statement,
                        type = mission.type,
                        codeLength = mission.codeLength,
                        isFinal = mission.isFinal,
                        state = mission.state,
                        solverId = mission.solvedBy?.userId,
                        solverUsername = mission.solvedBy?.username,
                        answers = mission.answers,
                        triggerType = mission.trigger.type,
                        triggerMissionId = mission.trigger.missionId,
                    )
                }
            }
            .distinctUntilChanged()
            .withFirestoreErrors()

    override suspend fun saveMission(
        gameId: String,
        missionId: String,
        title: String,
        statement: String,
        type: String,
        answers: List<String>,
        triggerType: String,
        triggerMissionId: String?,
        isFinal: Boolean,
    ) {
        val callable = services().functions.httpsCallable(SAVE_MISSION)
        val request = SaveMissionRequest(
            gameId = gameId,
            missionId = missionId,
            title = title,
            statement = statement,
            type = type,
            answers = answers,
            trigger = TriggerRequest(type = triggerType, missionId = triggerMissionId),
            isFinal = isFinal,
        )
        functionCall { callable.invoke(request) }
    }

    override suspend fun deleteMission(gameId: String, missionId: String) =
        callOnMission(name = DELETE_MISSION, gameId = gameId, missionId = missionId)

    override suspend fun unlockMission(gameId: String, missionId: String) =
        callOnMission(name = UNLOCK_MISSION, gameId = gameId, missionId = missionId)

    override suspend fun validateMission(gameId: String, missionId: String) =
        callOnMission(name = VALIDATE_MISSION, gameId = gameId, missionId = missionId)

    override suspend fun submitAnswer(gameId: String, missionId: String, answer: String): Boolean {
        val callable = services().functions.httpsCallable(SUBMIT_ANSWER)
        val request = SubmitAnswerRequest(gameId = gameId, missionId = missionId, answer = answer)
        return functionCall { callable.invoke(request).data<SubmitAnswerResponse>() }.correct
    }

    private suspend fun callOnMission(name: String, gameId: String, missionId: String) {
        val callable = services().functions.httpsCallable(name)
        functionCall { callable.invoke(MissionRequest(gameId = gameId, missionId = missionId)) }
    }

    private companion object {
        const val GAMES = "games"
        const val MISSIONS = "missions"
        const val MASTER_MISSIONS = "masterMissions"
        const val CREATED_AT = "createdAt"
        const val SAVE_MISSION = "saveMission"
        const val DELETE_MISSION = "deleteMission"
        const val UNLOCK_MISSION = "unlockMission"
        const val VALIDATE_MISSION = "validateMission"
        const val SUBMIT_ANSWER = "submitAnswer"
    }
}
