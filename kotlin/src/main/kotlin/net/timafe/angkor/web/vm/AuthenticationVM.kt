package net.timafe.angkor.web.vm

import net.timafe.angkor.domain.User

/**
 * Authentication ViewModel
 *
 * Communicates authStatus, user details and (if present) JWT to the frontend
 */
data class AuthenticationVM(
    val authenticated: Boolean,
    val user: User?,
    val idToken: String?
)
