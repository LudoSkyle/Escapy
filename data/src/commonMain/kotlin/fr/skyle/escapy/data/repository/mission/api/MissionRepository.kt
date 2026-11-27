package fr.skyle.escapy.data.repository.mission.api

import fr.skyle.escapy.data.model.StoredMission
import kotlinx.coroutines.flow.Flow

interface MissionRepository {

    /**
     * The missions of a game the players see, unlocked, in the order they were created, then each
     * change. Throws AccessDeniedException once the user is no longer a member.
     */
    fun watchMissions(gameId: String): Flow<List<StoredMission>>

    /**
     * Every mission of a game, locked ones and answers included, for its game master only: the rules
     * refuse anyone else, and a refused observation delays the others on Android.
     */
    fun watchMasterMissions(gameId: String): Flow<List<StoredMission>>

    /** Creates or changes a mission of a game the signed-in user runs; [missionId] is new for a creation. */
    suspend fun saveMission(
        gameId: String,
        missionId: String,
        title: String,
        statement: String,
        type: String,
        answers: List<String>,
        triggerType: String,
        triggerMissionId: String?,
        isFinal: Boolean,
    )

    suspend fun deleteMission(gameId: String, missionId: String)

    suspend fun unlockMission(gameId: String, missionId: String)

    suspend fun validateMission(gameId: String, missionId: String)

    /** Sends an answer to a mission; returns whether the server found it right. */
    suspend fun submitAnswer(gameId: String, missionId: String, answer: String): Boolean
}
