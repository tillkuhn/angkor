package net.timafe.angkor.service

import net.minidev.json.JSONArray
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.AuthScoped
import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.repo.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


/*
* Sample Auth Token
    "sub" : "3913****-****-****-****-****hase8b9c",
    "cognito:groups" : [ "eu-central-1_ILJadY8m3_Facebook", "angkor-admins" ],
    "cognito:preferred_role" : "arn:aws:iam::06********:role/angkor-cognito-role-admin",
    "cognito:roles" : [ "arn:aws:iam::06********:role/angkor-cognito-role-admin" ],
    "cognito:username" : "Facebook_16************65",
    "given_name" : "Gin",
    "name" : "Gin Tonic",
    "family_name" : "Tonic",
    "email" : "gin.tonic@gmail.com"
    "nonce" : "HIaFHPVbRPM1l3hase-****-****",
    "iss" : "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_ILJ******",
    "aud" : [ "20hase*********" ]
*/
@Service
class AuthService(
    // private val mapper: ObjectMapper,
    private val userRepository: UserRepository
) : ApplicationListener<AuthenticationSuccessEvent> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    var uuidRegex = Regex("""^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$""")

    var currentUser: User? = null

    /**
     * Let the authentication magic kick in when we receive AuthenticationSuccessEvent
     */
    override fun onApplicationEvent(event: AuthenticationSuccessEvent) {
        val auth: Authentication = event.authentication

        log.debug("AuthenticationSuccess class: ${auth.javaClass}")
        if (auth is OAuth2LoginAuthenticationToken) {
            val attributes = auth.principal.attributes
            log.info("User${auth.name} has authorities ${auth.authorities} attributes $attributes")

            val sub = attributes["sub"] as String
            // val sid = attributes["sid"] as String?
            val email = attributes["email"] as String?
            val cognitoUsername = attributes[Constants.COGNITO_USERNAME_KEY] as String?
            val roles = getRolesFromClaims(attributes)

            var id: UUID? = null
            if (uuidRegex.matches(sub)) {
                log.trace("subject $sub is UUID")
                id = UUID.fromString(sub)
            }
            val login = cognitoUsername ?: sub
            val users = userRepository.findByLoginOrEmailOrId(login.toLowerCase(), email?.toLowerCase(), id)
            log.debug("Check if user already exists login=$login (sub) email=$email id=$id result = ${users.size}")
            if (users.isEmpty()) {
                // Create new user in local db, re-use UUID from sub if it was set, otherwise create a random one
                if (id == null) {
                    id = UUID.randomUUID()
                }
                log.info("Creating new local db user $id (sub=$sub)")
                val newUser = User(id = id,
                    login = login, email = email, firstName = attributes["given_name"] as String?,
                    lastName = attributes["family_name"] as String?, name = attributes["name"] as String?,
                    lastLogin = LocalDateTime.now(), roles = ArrayList<String>(roles)
                )
                this.currentUser = userRepository.save(newUser)
            } else {
                if (users.size > 1) {
                    log.warn("Found ${users.size} users matching login=$login (sub) email=$email id=$id, expected 1")
                }
                log.info("Updating existing User $login")
                users[0].lastLogin = LocalDateTime.now()
                users[0].roles = ArrayList<String>(roles)
                this.currentUser = userRepository.save(users[0])
            }
        } else {
            log.warn("User authenticated by a non OAuth2 mechanism. AuthClass is ${auth.javaClass}")
        }
    }

    /**
     * Returns true if user is not authenticated, i.e. bears the AnonymousAuthenticationToken
     * as opposed to OAuth2AuthenticationToken
     */
    fun isAnonymous(): Boolean = SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken
    fun isAuthenticated(): Boolean = !isAnonymous()

    /**
     * Checks if the authscope of the item argument is part of allowedAuthScopes for the current user
     */
    fun allowedToAccess(item: AuthScoped): Boolean {
        val allowed = item.authScope in allowedAuthScopes()
        if (!allowed) {
            log.warn("current user not allowed to access ${item.authScope} ${item.javaClass.simpleName} item")
        }
        return allowed
    }

    /**
     * Returns a list of AuthScopes (PUBLIC,ALL_AUTH) the user is allows to access
     */
    fun allowedAuthScopes(): List<AuthScope> =
        if (isAnonymous()) listOf(AuthScope.PUBLIC) else listOf(AuthScope.PUBLIC, AuthScope.ALL_AUTH, AuthScope.PRIVATE)

    fun allowedAuthScopesAsString(): String = authScopesAsString(allowedAuthScopes())

    companion object {
        /**
         * helper to convert AuthScope enum list into a String array which can be used in NativeSQL Postgres queries
         * return example: {"PUBLIC", "PRIVATE"}
         */
        fun authScopesAsString(authScopes: List<AuthScope>): String {
            return "{" + authScopes.joinToString { "\"${it.name}\"" } + "}"
        }
    }

    /**
     * Map authorities from "groups" or "roles" claim in ID Token.
     *
     * @return a [GrantedAuthoritiesMapper] that maps groups from
     * the IdP to Spring Security Authorities.
     */
    @Bean
    fun userAuthoritiesMapper() =
        // Instantiate Interface GrantedAuthoritiesMapper and return
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
        val claimRoles = getRolesFromClaims(claims)
        // take a list of simple names role strings,
        // and map it into a list of GrantedAuthority objects if pattern matches
        return claimRoles.filter { it.startsWith("ROLE_") }
            .map { SimpleGrantedAuthority(it) }
    }

    @Suppress("UNCHECKED_CAST")
    fun getRolesFromClaims(claims: Map<String, Any>): Collection<String> {
        // Cognito groups  same same but different ...
        return if (claims.containsKey(Constants.COGNITO_ROLE_KEY /* cognito:roles */)) {
            when (val cognitoRoles = claims[Constants.COGNITO_ROLE_KEY]) {
                is JSONArray -> extractRolesFromJSONArray(cognitoRoles)
                else -> listOf()
            }
        } else {
            log.warn("JWT Claim does not contain ${Constants.COGNITO_ROLE_KEY}")
            listOf()
        }
    }

    // roles look like arn:aws:iam::06*******:role/angkor-cognito-role-guest
    // so we just take the last part after cognito-role- and uppercase it, e.g.
    fun extractRolesFromJSONArray(jsonArray: JSONArray): List<String> {
        val iamRolePattern = "cognito-role-"
        return jsonArray
            .filter { it.toString().contains(iamRolePattern) }
            .map {
                "ROLE_" + it.toString().substring(it.toString().indexOf(iamRolePattern) + iamRolePattern.length)
                    .toUpperCase()
            }
    }

}
