package net.timafe.angkor.security

import com.fasterxml.jackson.databind.ObjectMapper
import net.minidev.json.JSONArray
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Authority
import net.timafe.angkor.domain.User
import net.timafe.angkor.repo.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/*
"sub" : "3913****-****-****-****-****bacc8b9c",
"cognito:groups" : [ "eu-central-1_ILJadY8m3_Facebook", "angkor-admins" ],
"cognito:preferred_role" : "arn:aws:iam::06********:role/angkor-cognito-role-admin",
"cognito:roles" : [ "arn:aws:iam::06********:role/angkor-cognito-role-admin" ],
"cognito:username" : "Facebook_16************65",
"given_name" : "Gin",
"name" : "Gin Tonic",
"family_name" : "Tonic",
"email" : "gin.tonic@gmail.com"
"nonce" : "HIaFHPVbRPM1l3Nleyi-****-****",
"iss" : "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_ILJ******",
"aud" : [ "20rdpl*********" ]
*/
@Service
class AuthService(
        private val mapper: ObjectMapper,
        private val userRepository: UserRepository

) : ApplicationListener<AuthenticationSuccessEvent> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    var currentUser: User? = null;

    override fun onApplicationEvent(event: AuthenticationSuccessEvent) {
        val auth: Authentication = event.authentication
        log.debug("Authentication class: ${auth.javaClass}")
        if (auth is OAuth2LoginAuthenticationToken) {
            val attributes = auth.principal.attributes
            // val details = auth.getDetails() as Map<String, Any>
            // log.info("User ${auth.name} logged in: $details")
            log.info("User${auth.name} has authorities ${auth.authorities}")
            log.info("User ${auth.name} has attributes $attributes")

            val sub = attributes.get("sub") as String
            val sid = attributes.get("sid") as String?
            val email = attributes.get("email") as String?
            val cognitoUsername = attributes.get(Constants.COGNITO_USERNAME_KEY) as String?
            val roles = getRolesFromClaims(attributes);

            // Derive
            val id = sid?: sub
            val login = cognitoUsername ?: sub
            val users = userRepository.findByLoginOrEmailOrId(login.toLowerCase(), email?.toLowerCase(), id)
            log.debug("Lookup user login=$login (sub) email=$email id=$id result = ${users.size}")
            if (users.size  < 1) {
                log.info("new user $sub")

                val newUser = User(id = id, login = login, email = email, firstName = attributes.get("given_name") as String?,
                        lastName = attributes.get("family_name") as String?, name = attributes.get("name") as String?,
                        lastLogin = LocalDateTime.now(), roles = ArrayList<String>(roles))
                this.currentUser = userRepository.save(newUser)
            } else {
                if (users.size > 1) {
                    log.warn("Found ${users.size} users matching login=$login (sub) email=$email id=$id, expected 1")
                }
                log.info("Updating User $login")
                users[0].lastLogin = LocalDateTime.now()
                users[0].roles = ArrayList<String>(roles)
                userRepository.save(users[0])
                this.currentUser = users[0]
            }

        } else {
            log.warn("User authenticated by a non OAuth2 mechanism. Class is ${auth.javaClass}")
        }
    }
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
    fun getUserFromAuthentication(authToken: AbstractAuthenticationToken): User {
        val attributes: Map<String, Any> =
                when (authToken) {
                    is OAuth2AuthenticationToken -> authToken.principal.attributes
                    // is JwtAuthenticationToken -> authToken.tokenAttributes
                    else -> throw IllegalArgumentException("AuthenticationToken is not OAuth2")
                }

        val user = getUser(attributes)
        // log.info(mapper.writeValueAsString(attributes))
        user.roles = authToken.authorities.asSequence()
                .map(GrantedAuthority::getAuthority)
                .map { Authority(name = it).name }
                .toMutableList()
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
        val claimRoles = getRolesFromClaims(claims)
        // take a list of simple names role strings,
        // and map it into a list of GrantedAuthority objects if pattern machtes
        return claimRoles.filter { it.startsWith("ROLE_") }
                .map{SimpleGrantedAuthority(it)}
    }

    @Suppress("UNCHECKED_CAST")
    fun getRolesFromClaims(claims: Map<String, Any>): Collection<String> {
        // Cognito groups gibts auch noch ...
        return if (claims.containsKey(Constants.COGNITO_ROLE_KEY)) {
            when (val coros = claims.get(Constants.COGNITO_ROLE_KEY)) {
                is JSONArray -> extractRolesFromJSONArray(coros)
                else -> listOf<String>()
            }
        } else {
            log.warn("${Constants.COGNITO_ROLE_KEY} not found in claims")
            listOf<String>()
        }
    }

    // roles look like arn:aws:iam::06*******:role/angkor-cognito-role-guest
    // so we just take the last part after cognito-role- and upercase it, e.g.
    fun extractRolesFromJSONArray(jsonArray: JSONArray): List<String> {
        val iamRolePattern = "cognito-role-"
        return jsonArray
                .filter { it.toString().contains(iamRolePattern) }
                .map { "ROLE_" + it.toString().substring(it.toString().indexOf(iamRolePattern) + iamRolePattern.length).toUpperCase() }

    }

    private fun getUser(details: Map<String, Any>): User {
        val user = User()
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
