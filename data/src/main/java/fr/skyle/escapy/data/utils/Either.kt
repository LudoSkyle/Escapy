package fr.skyle.escapy.data.utils

sealed interface Either<out E : EitherFailure, out S> {
    data class Failure<out E : EitherFailure>(val error: E) : Either<E, Nothing>
    data class Success<out S>(val value: S) : Either<Nothing, S>
}

fun <E : EitherFailure, S> Either<E, S>.getOrThrow(): S {
    return when (this) {
        is Either.Success -> value
        is Either.Failure -> throw error.toException()
    }
}

interface EitherFailure {
    fun toException(): Exception
}
