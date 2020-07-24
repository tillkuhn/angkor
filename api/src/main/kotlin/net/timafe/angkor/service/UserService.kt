package net.timafe.angkor.service

import net.timafe.angkor.domain.Authority
import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserDTO
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service

@Service
class UserService {

    /**
     * Returns the user from an OAuth 2.0 login or resource server with JWT.
     * Synchronizes the user in the local repository.
     *
     * @param authToken the authentication token.
     * @return the user from the authentication.
     */
    fun getUserFromAuthentication(authToken: AbstractAuthenticationToken): UserDTO {
        val attributes: Map<String, Any> =
                when (authToken) {
                    is OAuth2AuthenticationToken -> authToken.principal.attributes
                    // is JwtAuthenticationToken -> authToken.tokenAttributes
                    else -> throw IllegalArgumentException("AuthenticationToken is not OAuth2")
                }

        val user = getUser(attributes)
        user.authorities = authToken.authorities.asSequence()
                .map(GrantedAuthority::getAuthority)
                .map { Authority(name = it).name }
                .toMutableSet()
        return user;
    }

    companion object {

        @JvmStatic
        private fun getUser(details: Map<String, Any>): UserDTO {
            val user = UserDTO()
            // handle resource server JWT, where sub claim is email and uid is ID
            if (details["uid"] != null) {
                user.id = details["uid"] as String
                user.login = details["sub"] as String
            } else {
                user.id = details["sub"] as String
            }
            if (details["preferred_username"] != null) {
                user.login = (details["preferred_username"] as String).toLowerCase()
            } else if (user.login == null) {
                user.login = user.id
            }
            if (details["given_name"] != null) {
                user.firstName = details["given_name"] as String
            }
            if (details["family_name"] != null) {
                user.lastName = details["family_name"] as String
            }
            if (details["email_verified"] != null) {
                user.activated = details["email_verified"] as Boolean
            }
            if (details["email"] != null) {
                user.email = (details["email"] as String).toLowerCase()
            } else {
                user.email = details["sub"] as String
            }
                     if (details["picture"] != null) {
                user.imageUrl = details["picture"] as String
            }
            user.activated = true
            return user
        }
    }


}
