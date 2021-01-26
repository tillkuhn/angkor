package net.timafe.angkor.domain.enums

enum class AuthScope {
    PUBLIC,
    ALL_AUTH,
    RESTRICTED,
    PRIVATE;

    // fun friendly() = name.toLowerCase().replace('_',' ')
}
