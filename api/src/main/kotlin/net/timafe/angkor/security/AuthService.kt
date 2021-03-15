package net.timafe.angkor.security

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.repo.UserRepository
import net.timafe.angkor.service.CacheService
import net.timafe.angkor.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


@Service
class AuthService(
    private val userService: UserService,
    private val cacheService: CacheService
) : ApplicationListener<AuthenticationSuccessEvent> {


    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Let the authentication magic kick in when we receive AuthenticationSuccessEvent
     */
    override fun onApplicationEvent(event: AuthenticationSuccessEvent) {
        val auth: Authentication = event.authentication

        log.debug("AuthenticationSuccess class: ${auth.javaClass}")
        if (auth !is OAuth2LoginAuthenticationToken) {
            val msg =
                "User authenticated by AuthClass=${auth.javaClass}, ${OAuth2LoginAuthenticationToken::class.java} is supported"
            log.error(msg)
            throw IllegalArgumentException(msg)
        }

        val attributes = auth.principal.attributes
        log.info("User${auth.name} has authorities ${auth.authorities} attributes $attributes")
//
        val sub = attributes["sub"] as String
        val email = attributes["email"] as String?
        val cognitoUsername = attributes[Constants.COGNITO_USERNAME_KEY] as String?
        val roles = SecurityUtils.getRolesFromClaims(attributes)
        var id: UUID? = SecurityUtils.extractUUIDfromSubject(sub)
        val login = cognitoUsername ?: sub
        val user = userService.findUser(attributes)
        // Let's ignore sid attribute for the time being

        log.debug("Check if user already exists login=$login (sub) email=$email id=$id result = $user")
        if (user == null) {
            // Create new user in local db, re-use UUID from sub if it was set, otherwise create a random one
            if (id == null) {
                id = UUID.randomUUID()
            }
            val name = if (attributes["name"] != null) attributes["name"] else login
            log.info("Creating new local db user $id (sub=$sub)")
            val newUser = User(
                id = id,
                login = login, email = email, firstName = attributes["given_name"] as String?,
                lastName = attributes["family_name"] as String?, name = name as String?,
                lastLogin = LocalDateTime.now(), roles = ArrayList<String>(roles)
            )
            userService.save(newUser)
        } else {
            log.info("Updating existing User $login")
            user.lastLogin = LocalDateTime.now()
            user.roles = ArrayList<String>(roles)
            userService.save(user)
        }
        // SecurityContextHolder.getContext().authentication is still null at this point
    }


    fun cleanCaches() {
        cacheService.clearCache(UserRepository.USER_SUMMARIES_CACHE)
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


    /** take a list of simple names role strings,
     * and map it into a list of GrantedAuthority objects if pattern matches
     */
    fun extractAuthorityFromClaims(claims: Map<String, Any>): List<GrantedAuthority> {
        val claimRoles = SecurityUtils.getRolesFromClaims(claims)
        return claimRoles.filter { it.startsWith("ROLE_") }
            .map { SimpleGrantedAuthority(it) }
    }



}
