package net.timafe.angkor.domain.enums

/**
 * Role constants, mainly used by SecurityUtils
 *
 * // Example Usage:
 *   val isAdmin = authorities.asSequence()
 *   .filter { it.authority.equals(AppRole.ADMIN.withRolePrefix) }
 *   .any { it.authority.equals(AppRole.ADMIN.withRolePrefix) }
 */
enum class AppRole {

    USER,
    ADMIN,
    ANONYMOUS;

    val withRolePrefix: String
        get() = "ROLE_$name"

}
