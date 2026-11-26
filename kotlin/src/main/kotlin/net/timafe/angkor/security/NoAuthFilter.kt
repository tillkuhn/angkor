package net.timafe.angkor.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant

/**
 * LOCAL DEVELOPMENT ONLY: Authenticates every request as the user configured in app.noauth.user-id
 * (with ROLE_USER and ROLE_ADMIN), so the UI can be tested without a Cognito login.
 * Added to the filter chain by [net.timafe.angkor.config.SecurityConfig] only with Spring profile "noauth"
 * (e.g. SPRING_PROFILES_ACTIVE=noauth), and never together with "prod".
 */
class NoAuthFilter(private val userId: String) : OncePerRequestFilter() {

    init {
        LoggerFactory.getLogger(javaClass).warn("[NoAuth] Profile noauth is active, all requests are authenticated as $userId")
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val claims = mapOf<String, Any>(SecurityUtils.JWT_SUBJECT_KEY to userId)
        val idToken = OidcIdToken("noauth-dummy-token", Instant.now(), Instant.now().plusSeconds(3600), claims)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN"))
        val principal = DefaultOidcUser(authorities, idToken)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities)
        filterChain.doFilter(request, response)
    }
}
