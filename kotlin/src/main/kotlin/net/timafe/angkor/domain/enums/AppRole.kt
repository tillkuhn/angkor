package net.timafe.angkor.domain.enums

enum class AppRole {

    USER,
    ADMIN,
    ANONYMOUS;

    val withRolePrefix: String
        get() = "ROLE_$name"
}
