package net.timafe.angkor

import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.enums.AppRole
import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.repo.UserRepository
import net.timafe.angkor.security.AuthSuccessListener
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.CacheService
import net.timafe.angkor.service.UserService
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser

/**
 * Test class for the Security Utility methods.
 */
class SecurityUtilsUnitTest {

    private val userService = UserService(
                Mockito.mock(UserRepository::class.java),
                Mockito.mock(CacheService::class.java)
    )

    // https://stackoverflow.com/questions/30305217/is-it-possible-to-use-mockito-in-kotlin
    private fun <T> any(): T {
        return Mockito.any<T>()
    }

    @Test
    fun testAuthListener() {
        //
        val userService = Mockito.mock(UserService::class.java)
        //doNothing().`when`(userService).createUser(any())
        //doNothing().`when`(userService).save(any())
        doNothing().`when`(userService).createUser(this.any()) // see private hack above
        // save(any()).thenReturn(Mockito.mock(User::class.java))

        val attributes = TestHelpers.somePrincipalAttributes()
        val idToken = OidcIdToken(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), attributes)
        val authorities = SecurityUtils.getRolesFromAttributes(attributes).map { SimpleGrantedAuthority(it) }
        // listOf(SimpleGrantedAuthority(AppRole.USER.withRolePrefix))
        val user = DefaultOidcUser(authorities, idToken)
        val oauthToken = OAuth2AuthenticationToken(user, authorities, "cognito") // or oidc
        // For later: Mock UserRepository.USERS_BY_LOGIN_CACHE]
        val clientReg = Mockito.mock(ClientRegistration::class.java)
        val authEx = Mockito.mock(OAuth2AuthorizationExchange::class.java)
        val accessToken = Mockito.mock(OAuth2AccessToken::class.java)
        val oauthLoginToken = OAuth2LoginAuthenticationToken(clientReg,authEx,user, authorities, accessToken) // or oidc

        val asl = AuthSuccessListener(userService)
        asl.onApplicationEvent(AuthenticationSuccessEvent(oauthLoginToken))
        verify(userService, times(1)).createUser(any())

        val auts = asl.extractAuthorityFromClaims(attributes);
        assertThat(auts.size).isGreaterThan(0)

    }

    @Test
    fun testAppRoles() {
        assertThat(AppRole.ADMIN.withRolePrefix).isEqualTo("ROLE_ADMIN")
        assertThat(AppRole.USER.withRolePrefix).isEqualTo("ROLE_USER")
        assertThat(AppRole.ANONYMOUS.withRolePrefix).isEqualTo("ROLE_ANONYMOUS")
    }

    @Test
    fun testConventionalUserPasswordAuth() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("admin", "admin1")
        SecurityContextHolder.setContext(securityContext)
        val login = SecurityUtils.getCurrentUserLogin()
        assertThat(login).contains("admin")
    }

    @Test
    fun testGetCurrentUserLoginForOAuth2() {
        val securityContext = SecurityContextHolder.createEmptyContext()

        val attributes = TestHelpers.somePrincipalAttributes()
        val idToken = OidcIdToken(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), attributes)
        val authorities = SecurityUtils.getRolesFromAttributes(attributes).map { SimpleGrantedAuthority(it) }
        // listOf(SimpleGrantedAuthority(AppRole.USER.withRolePrefix))
        val user = DefaultOidcUser(authorities, idToken)
        val oauthToken = OAuth2AuthenticationToken(user, authorities, "cognito") // or oidc
        // For later: Mock UserRepository.USERS_BY_LOGIN_CACHE]
        val clientReg = Mockito.mock(ClientRegistration::class.java)
        val authEx = Mockito.mock(OAuth2AuthorizationExchange::class.java)
        val accessToken = Mockito.mock(OAuth2AccessToken::class.java)
        val oauthLoginToken = OAuth2LoginAuthenticationToken(clientReg,authEx,user, authorities, accessToken) // or oidc

        securityContext.authentication = oauthToken
        SecurityContextHolder.setContext(securityContext)
        val expectedLogin = attributes.get(SecurityUtils.COGNITO_USERNAME_KEY).toString()
        val login = SecurityUtils.getCurrentUserLogin()
        assertThat(login).contains(expectedLogin)

        val extractedAttributes = userService.extractAttributesFromAuthToken(oauthLoginToken)
        assertThat(extractedAttributes["sub"].toString()).contains(attributes["sub"].toString())
        assertThat(SecurityUtils.isAuthenticated()).isTrue
        assertThat(SecurityUtils.isAnonymous()).isFalse
        assertThat(SecurityUtils.safeConvertToUUID(attributes["sub"].toString())).isNotNull
        assertThat(SecurityUtils.allowedAuthScopesAsString()).isEqualTo("""{"PUBLIC", "ALL_AUTH", "RESTRICTED", "PRIVATE"}""")
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
