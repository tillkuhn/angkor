package net.timafe.angkor.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.el.parser.AstMapEntry
import net.minidev.json.JSONArray
import net.timafe.angkor.domain.Authority
import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.stereotype.Service

@Service
class AuthService(private val mapper: ObjectMapper) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    fun isAnonymous(): Boolean {
        val auth: Authentication = SecurityContextHolder.getContext().authentication;
        //  anonymous: org.springframework.security.authentication.AnonymousAuthenticationToken@b7d78d14:
        //      Principal: anonymousUser;
        // logged in: org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken@9b65b523:
        //     Principal: Name: [Facebook_145501.....], Granted Authorities: [[ROLE_USER, SCOPE_openid]],
        return auth is AnonymousAuthenticationToken
    }

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
        // log.info(mapper.writeValueAsString(attributes))
        user.authorities = authToken.authorities.asSequence()
                .map(GrantedAuthority::getAuthority)
                .map { Authority(name = it).name }
                .toMutableSet()
        return user;
    }

    /**
     * Map authorities from "groups" or "roles" claim in ID Token.
     *
     * @return a [GrantedAuthoritiesMapper] that maps groups from
     * the IdP to Spring Security Authorities.
     */
    @Bean
    fun userAuthoritiesMapper() =
        GrantedAuthoritiesMapper { authorities ->
            val mappedAuthorities = mutableSetOf<GrantedAuthority>()
            authorities.forEach { authority ->
                if (authority is OidcUserAuthority) {
                    val grantedList = extractAuthorityFromClaims(authority.idToken.claims)
                    mappedAuthorities.addAll(grantedList)
                }
            }
            mappedAuthorities
        }


    fun extractAuthorityFromClaims(claims: Map<String, Any>): List<GrantedAuthority> {
        return mapRolesToGrantedAuthorities(getRolesFromClaims(claims))
    }

    // take a list of simple names role strings, and map it into a list of GrantedAuthority objects if pattern machtes
    fun mapRolesToGrantedAuthorities(roles: Collection<String>): List<GrantedAuthority> {
        return roles
                .filter { it.startsWith("ROLE_") }
                .map { SimpleGrantedAuthority(it) }
    }


    @Suppress("UNCHECKED_CAST")
    fun getRolesFromClaims(claims: Map<String, Any>): Collection<String> {
        // Cognito groups gibts auch noch ...
        return if (claims.containsKey("cognito:roles")) {
            when (val coros = claims.get("cognito:roles")) {
                is JSONArray -> extractRolesFromJSONArray(coros)
                else -> listOf<String>()
            }
        } else {
            listOf<String>()
        }
    }

    // roles look like arn:aws:iam::06*******:role/angkor-cognito-role-guest
    // so we just take the last part after cognito-role-
    fun extractRolesFromJSONArray(jsonArray: JSONArray): List<String> {
        val iamRolePattern = "cognito-role-"
        return jsonArray
                .filter { it.toString().contains(iamRolePattern) }
                .map { "ROLE_" + it.toString().substring(it.toString().indexOf(iamRolePattern) + iamRolePattern.length).toUpperCase() }

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
            if (details["name"] != null) {
                user.name = details["name"] as String
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
