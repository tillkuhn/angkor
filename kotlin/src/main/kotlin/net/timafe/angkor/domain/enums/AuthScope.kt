package net.timafe.angkor.domain.enums

import java.util.*

/**
 * Authentication Scope Enum to control Permissions and Access to Entities
 */
enum class AuthScope {

    PUBLIC,
    ALL_AUTH,
    RESTRICTED,
    PRIVATE;

    fun friendlyName() = name.lowercase().replace('_', ' ')
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
