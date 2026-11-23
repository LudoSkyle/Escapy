package fr.skyle.escapy.data.utils

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import fr.skyle.escapy.data.enums.AuthProvider
import fr.skyle.escapy.data.ext.awaitWithTimeout
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

fun AuthResult.requireUser(): FirebaseUser =
    requireNotNull(user) { "Firebase user is null" }

fun FirebaseUser.getAuthProvider(): AuthProvider? {
    // Exclude the first element: it is ALWAYS FirebaseAuthProvider.PROVIDER_ID
    val realProvider = providerData
        .map { it.providerId }
        .firstOrNull { it != FirebaseAuthProvider.PROVIDER_ID }

    return AuthProvider.fromProviderId(realProvider)
}

suspend fun <T> Query.readOnce(clazz: Class<T>): T? {
    val path = this.toString()
    Timber.i("ℹ️ READ → path=$path")

    val snapshot = get().addOnFailureListener { e ->
        Timber.e(e, "🟥 READ → path=$path")
    }.awaitWithTimeout()

    val value = snapshot.getValue(clazz)
    Timber.i("🟩 READ → path=$path\ndata=$value")

    return value
}

suspend fun <T> Query.readOnce(typeIndicator: GenericTypeIndicator<T>): T? {
    val path = this.toString()
    Timber.i("ℹ️ READ → path=$path")

    val snapshot = get()
        .addOnFailureListener { e ->
            Timber.e(e, "🟥 READ → path=$path")
        }.awaitWithTimeout()

    val value = snapshot.getValue(typeIndicator)
    Timber.i("🟩 READ → path=$path\ndata=$value")

    return value
}

fun <T> Query.listenAsFlow(typeIndicator: GenericTypeIndicator<T>): Flow<T?> =
    callbackFlow {
        val path = this@listenAsFlow.toString()
        Timber.i("ℹ️ LISTEN → path=$path")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(typeIndicator)
                Timber.i("🟩 LISTEN → path=$path\ndata=$value")
                trySend(value).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "🟥 LISTEN → path=$path")
                close(error.toException())
            }
        }

        this@listenAsFlow.addValueEventListener(listener)

        awaitClose {
            this@listenAsFlow.removeEventListener(listener)
            Timber.i("⬜ LISTEN canceled → path=$path")
        }
    }

// ---------------- WRITE ----------------

suspend fun DatabaseReference.writeOnce(data: Any) {
    val path = this.toString()
    Timber.i("ℹ️ WRITE → path=$path\ndata=$data")

    setValue(data)
        .addOnFailureListener { e ->
            Timber.e(e, "🟥 WRITE → path=$path")
        }
        .awaitWithTimeout()

    Timber.i("🟩 WRITE → path=$path")
}

// ---------------- UPDATE ----------------

suspend fun DatabaseReference.updateOnce(data: Map<String, Any?>) {
    val path = this.toString()
    Timber.i("ℹ️ UPDATE → path=$path\ndata=$data")

    updateChildren(data)
        .addOnFailureListener { e ->
            Timber.e(e, "🟥 UPDATE → path=$path")
        }
        .awaitWithTimeout()

    Timber.i("🟩 UPDATE → path=$path")
}