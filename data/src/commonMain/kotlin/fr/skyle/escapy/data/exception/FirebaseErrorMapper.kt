package fr.skyle.escapy.data.exception

import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.functions.FirebaseFunctionsException
import dev.gitlive.firebase.functions.FunctionsExceptionCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/** The failure of an error code of Firebase Auth, the same on Android and iOS. */
internal fun authFailureOf(errorCode: String?): AuthFailure =
    when (errorCode) {
        "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL", "ERROR_USER_DISABLED",
        -> AuthFailure.INVALID_CREDENTIALS
        "ERROR_EMAIL_ALREADY_IN_USE" -> AuthFailure.EMAIL_ALREADY_IN_USE
        "ERROR_CREDENTIAL_ALREADY_IN_USE" -> AuthFailure.CREDENTIAL_ALREADY_IN_USE
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> AuthFailure.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL
        "ERROR_USER_MISMATCH" -> AuthFailure.USER_MISMATCH
        "ERROR_WEAK_PASSWORD" -> AuthFailure.WEAK_PASSWORD
        "ERROR_INVALID_EMAIL" -> AuthFailure.INVALID_EMAIL
        "ERROR_TOO_MANY_REQUESTS" -> AuthFailure.TOO_MANY_REQUESTS
        "ERROR_REQUIRES_RECENT_LOGIN" -> AuthFailure.RECENT_SIGN_IN_REQUIRED
        else -> AuthFailure.UNKNOWN
    }

/** The reason a Cloud Function sent in the details of its refusal. */
internal fun functionReasonOf(details: Any?): String? =
    (details as? Map<*, *>)?.get("reason") as? String

/** Runs a call to Firebase Auth, its errors turned into the exceptions of this module. */
internal suspend fun <T> authCall(block: suspend () -> T): T =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: FirebaseAuthException) {
        if (exception.errorCode == "ERROR_NETWORK_REQUEST_FAILED") {
            throw NetworkUnavailableException(cause = exception)
        }
        throw AuthFailureException(failure = authFailureOf(exception.errorCode), cause = exception)
    } catch (exception: FirebaseNetworkException) {
        throw NetworkUnavailableException(cause = exception)
    } catch (exception: FirebaseTooManyRequestsException) {
        throw AuthFailureException(failure = AuthFailure.TOO_MANY_REQUESTS, cause = exception)
    }

/** Runs a call to a Cloud Function, its errors turned into the exceptions of this module. */
internal suspend fun <T> functionCall(block: suspend () -> T): T =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: FirebaseFunctionsException) {
        when (exception.code) {
            FunctionsExceptionCode.UNAVAILABLE, FunctionsExceptionCode.DEADLINE_EXCEEDED ->
                throw NetworkUnavailableException(cause = exception)
            else -> throw FunctionFailureException(reason = functionReasonOf(exception.details), cause = exception)
        }
    } catch (exception: FirebaseNetworkException) {
        throw NetworkUnavailableException(cause = exception)
    }

/**
 * An observation of Firestore, a refusal turned into AccessDeniedException: Firestore stops an
 * observation once its rules no longer let the user read, such as a player excluded from a game.
 */
internal fun <T> Flow<T>.withFirestoreErrors(): Flow<T> =
    catch { exception ->
        if (exception is FirebaseFirestoreException && exception.code == FirestoreExceptionCode.PERMISSION_DENIED) {
            throw AccessDeniedException(cause = exception)
        }
        throw exception
    }
