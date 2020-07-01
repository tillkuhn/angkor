package net.timafe.angkor.service

import net.timafe.angkor.domain.User
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
    fun getUserFromAuthentication(authToken: AbstractAuthenticationToken): User {
        /*
        val attributes: Map<String, Any> =
                when (authToken) {
                    is OAuth2AuthenticationToken -> authToken.principal.attributes
                    is JwtAuthenticationToken -> authToken.tokenAttributes
                    else -> throw IllegalArgumentException("AuthenticationToken is not OAuth2 or JWT!")
                }

        val user = getUser(attributes)
        user.authorities = authToken.authorities.asSequence()
                .map(GrantedAuthority::getAuthority)
                .map { Authority(name = it) }
                .toMutableSet()
        return UserDTO(syncUserWithIdP(attributes, user))
         */
        return User(name = authToken.name)
    }
}
