package net.timafe.angkor.security

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


@Service
class AuthService(
    private val userService: UserService,
    private val cacheService: CacheService
) : ApplicationListener<AuthenticationSuccessEvent> {


    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Let the authentication magic kick in when we receive AuthenticationSuccessEvent
     *
     * auth should be of type OAuth2LoginAuthenticationToken which contains the same elements as
     * Oauth2AuthenticationToken (e.g. principal {DefaultOidcUser}), see SecurityUtils for documentation
     */
    override fun onApplicationEvent(event: AuthenticationSuccessEvent) {
        val auth: Authentication = event.authentication

        if (auth !is OAuth2LoginAuthenticationToken) {
            val msg = "User authenticated by ${auth.javaClass}, expects ${OAuth2LoginAuthenticationToken::class.java}"
            log.error(msg)
            throw IllegalArgumentException(msg)
        }

        val attributes = auth.principal.attributes
        log.info("User ${auth.name} has authorities ${auth.authorities} attributes $attributes")
        val user = userService.findUser(attributes)

        if (user == null) {
            userService.createUser(attributes)
        } else {
            log.info("Updating existing DB User $user")
            user.lastLogin = LocalDateTime.now()
            user.roles = ArrayList<String>(SecurityUtils.getRolesFromClaims(attributes))
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
