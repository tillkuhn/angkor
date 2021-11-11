package net.timafe.angkor.domain.enums

/**
 * Role constants, mainly used by SecurityUtils
 */
enum class AppRole {

    USER,
    ADMIN,
    ANONYMOUS;

    val withRolePrefix: String
        get() = "ROLE_$name"

}
