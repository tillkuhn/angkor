package net.timafe.angkor

import net.timafe.angkor.domain.enums.AppRole
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import java.util.*

/**
 * Test class for the Security Utility methods.
 */
class SecurityUtilsUnitTest {

    @Test
    fun testAppRoles() {
        assertThat(AppRole.ADMIN.withRolePrefix).isEqualTo("ROLE_ADMIN")
        assertThat(AppRole.USER.withRolePrefix).isEqualTo("ROLE_USER")
        assertThat(AppRole.ANONYMOUS.withRolePrefix).isEqualTo("ROLE_ANONYMOUS")
    }

    @Test
    fun testGetCurrentUserLogin() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin")
        SecurityContextHolder.setContext(securityContext)
        val login = getCurrentUserLogin()
        assertThat(login).contains("admin")
    }

    @Test
    fun testGetCurrentUserLoginForOAuth2() {
        val securityContext = SecurityContextHolder.createEmptyContext()

        val claims = mapOf(
            "groups" to AppRole.USER.withRolePrefix,
            "sub" to 123886655,
            "preferred_username" to "admin"
        )
        val idToken = OidcIdToken(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), claims)
        val authorities = listOf(SimpleGrantedAuthority(AppRole.USER.withRolePrefix))
        val user = DefaultOidcUser(authorities, idToken)
        val oauthToken = OAuth2AuthenticationToken(user, authorities, "cognito") // or oidc
        // For later: Mock UserRepository.USERS_BY_LOGIN_CACHE]
        val clientReg = Mockito.mock(ClientRegistration::class.java)
        val authEx = Mockito.mock(OAuth2AuthorizationExchange::class.java)
        val accessToken = Mockito.mock(OAuth2AccessToken::class.java)
        val oauthLoginToken = OAuth2LoginAuthenticationToken(clientReg,authEx,user, authorities, accessToken) // or oidc

        securityContext.authentication = oauthToken
        SecurityContextHolder.setContext(securityContext)

        val login = getCurrentUserLogin()

        assertThat(login).contains("admin")
    }

    @Test
    fun testIsAuthenticated() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin")
        SecurityContextHolder.setContext(securityContext)
        val isAuthenticated = isAuthenticated()
        assertThat(isAuthenticated).isTrue
    }

    @Test
    fun testAnonymousIsNotAuthenticated() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(SimpleGrantedAuthority(AppRole.ANONYMOUS.withRolePrefix))
        securityContext.authentication = UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities)
        SecurityContextHolder.setContext(securityContext)
        val isAuthenticated = isAuthenticated()
        assertThat(isAuthenticated).isFalse
    }

    @Test
    fun testIsCurrentUserInRole() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(SimpleGrantedAuthority(AppRole.USER.withRolePrefix))
        securityContext.authentication = UsernamePasswordAuthenticationToken("user", "user", authorities)
        SecurityContextHolder.setContext(securityContext)

        assertThat(isCurrentUserInRole(AppRole.USER.withRolePrefix)).isTrue
        assertThat(isCurrentUserInRole(AppRole.ADMIN.withRolePrefix)).isFalse
    }

    // ***************************
    // From Security Utils
    // ***************************
    fun getCurrentUserLogin(): Optional<String> =
        Optional.ofNullable(extractPrincipal(SecurityContextHolder.getContext().authentication))

    fun extractPrincipal(authentication: Authentication?): String? {

        if (authentication == null) {
            return null
        }

        return when (val principal = authentication.principal) {
            is UserDetails -> principal.username
            // is JwtAuthenticationToken -> (authentication as JwtAuthenticationToken).token.claims as String
            is DefaultOidcUser -> {
                if (principal.attributes.containsKey("preferred_username")) {
                    principal.attributes["preferred_username"].toString()
                } else {
                    null
                }
            }
            is String -> principal
            else -> null
        }
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise.
     */
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication != null) {
            val isAnonymousUser = getAuthorities(authentication)?.none { it == AppRole.ANONYMOUS.withRolePrefix }
            if (isAnonymousUser != null) {
                return isAnonymousUser
            }
        }

        return false
    }
    /**
     * If the current user has a specific authority (security role).
     *
     * The name of this method comes from the `isUserInRole()` method in the Servlet API
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
    fun isCurrentUserInRole(authority: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication != null) {
            val isUserPresent = getAuthorities(authentication)?.any { it == authority }
            if (isUserPresent != null) {
                return isUserPresent
            }
        }

        return false
    }

    fun getAuthorities(authentication: Authentication): List<String>? {
        return authentication.authorities?.map(GrantedAuthority::getAuthority)
    }

}
