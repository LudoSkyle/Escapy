package fr.skyle.escapy.domain.validation

/** A rule of the policy of Escapy for a new password. */
enum class PasswordRule {
    MIN_LENGTH,
    UPPERCASE,
    DIGIT,
    SPECIAL_CHARACTER,
}

/** The policy of a new password, the same at sign-up, when linking an account and when changing it. */
object PasswordPolicy {

    const val MIN_LENGTH = 8

    /** The rules [password] breaks, none when it can be used. */
    fun missingRules(password: String): Set<PasswordRule> =
        buildSet {
            if (password.length < MIN_LENGTH) add(PasswordRule.MIN_LENGTH)
            if (password.none(Char::isUpperCase)) add(PasswordRule.UPPERCASE)
            if (password.none(Char::isDigit)) add(PasswordRule.DIGIT)
            if (password.all(Char::isLetterOrDigit)) add(PasswordRule.SPECIAL_CHARACTER)
        }
}
