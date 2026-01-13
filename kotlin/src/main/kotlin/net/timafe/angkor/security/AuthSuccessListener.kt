package net.timafe.angkor.security

import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.service.EventService
import net.timafe.angkor.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

/**
 * Let the authentication magic kick in when we receive AuthenticationSuccessEvent
 */
@Service
class AuthSuccessListener(
    private val userService: UserService,
    private val eventService: EventService
) : ApplicationListener<AuthenticationSuccessEvent> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * auth should be of type OAuth2LoginAuthenticationToken which contains the same elements as
     * Oauth2AuthenticationToken (e.g. principal {DefaultOidcUser}), see SecurityUtils for documentation
     *
     * WARNING: SecurityContextHolder.getContext().authentication is still null at this point
     */
    override fun onApplicationEvent(event: AuthenticationSuccessEvent) {
        val auth: Authentication = event.authentication
        if (auth is UsernamePasswordAuthenticationToken) {
            log.trace("Special treatment for UsernamePasswordAuthenticationToken {}", auth.principal)
            return
        } else if (auth !is OAuth2LoginAuthenticationToken) {
            throw  IllegalArgumentException("AuthenticationToken is not OAuth2 but ${auth.javaClass}!")
        }
        val attributes = userService.extractAttributesFromAuthToken(auth)
        log.info("[User] ${auth.name} authorities=${auth.authorities} attributes=$attributes")
        val user = userService.findUser(attributes)

        val userId = SecurityUtils.safeConvertToUUID(attributes[SecurityUtils.JWT_SUBJECT_KEY] as String?)
        val message: String

        if (user == null) {
            userService.createUser(attributes)
            message = "New user logged in userId=${userId}"
        } else {
            log.info("Updating existing DB User $user")
            user.lastLogin = ZonedDateTime.now()
            user.roles = ArrayList<String>(SecurityUtils.getRolesFromAttributes(attributes))
            userService.save(user)
            message = "Existing user logged in userId=${userId}"
        }
        val em = Event(
            action = "login:user",
            message = message,
            entityId = userId,
            userId = userId,
            source = this.javaClass.simpleName,
            )
        eventService.publish(EventTopic.AUDIT, em)
    }

    /**
     * Map authorities from "groups" or "roles" claim in ID Token.
     *
     * @return a [GrantedAuthoritiesMapper] that maps groups from
     * the IdP to Spring Security Authorities.
     *
     * GrantedAuthoritiesMapper: Mapping interface which can be injected into the authentication layer to convert the
     * authorities loaded from storage into those which will be used in the Authentication object.
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


    /**
     * take a list of simple named role strings,
     * and map it into a list of GrantedAuthority objects if pattern matches
     */
    fun extractAuthorityFromClaims(claims: Map<String, Any>): List<GrantedAuthority> {
        val claimRoles = SecurityUtils.getRolesFromAttributes(claims)
        return claimRoles.filter { it.startsWith("ROLE_") }
            .map { SimpleGrantedAuthority(it) }
    }

}
