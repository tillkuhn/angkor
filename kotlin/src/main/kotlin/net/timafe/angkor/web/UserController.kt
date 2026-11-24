package net.timafe.angkor.web

import jakarta.servlet.http.HttpServletRequest
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserSummary
import net.timafe.angkor.repo.UserRepository
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.UserService
import net.timafe.angkor.web.vm.AuthenticationVM
import net.timafe.angkor.web.vm.BooleanResult
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping(Constants.API_LATEST)
class UserController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
    private val jdbcTemplate: JdbcTemplate,
) {

    internal class AccountResourceException(message: String) : RuntimeException(message)

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Can be used by frontend to check if the current user is authenticated
     * (Current SecurityContext != AnonymousAuthenticationToken)
     */
    @GetMapping("/authenticated")
    fun isAuthenticated(): BooleanResult {
        return BooleanResult(SecurityUtils.isAuthenticated())
    }

    @GetMapping("/authentication")
    fun getAuthentication(authToken: Principal?, request: HttpServletRequest): AuthenticationVM {
        if (!SecurityUtils.isAuthenticated()) {
            return AuthenticationVM(authenticated = false, user = null, idToken = null)
        }
        if (authToken !is AbstractAuthenticationToken) {
            throw IllegalArgumentException("AbstractAuthenticationToken expected, UserController can't handle ${authToken?.javaClass}!")
        }
        if (authToken is OAuth2AuthenticationToken && !refreshTokensIfExpired(authToken)) {
            log.info("[User] Tokens for ${authToken.name} can no longer be refreshed, invalidating session")
            request.getSession(false)?.invalidate()
            SecurityContextHolder.clearContext()
            return AuthenticationVM(authenticated = false, user = null, idToken = null)
        }
        // a token refresh replaces the authentication in the security context (incl. a fresh ID token)
        val currentToken = SecurityContextHolder.getContext().authentication as? AbstractAuthenticationToken ?: authToken
        return AuthenticationVM(
            authenticated = true,
            user = getCurrentUser(currentToken),
            idToken = userService.extractIdTokenFromAuthToken(currentToken)
        )
    }

    /**
     * Uses the refresh token to obtain new tokens from the IdP if the current access token has expired.
     * Called whenever the frontend (re)loads, so long-lived sessions are re-validated with Cognito at least daily.
     *
     * @return false if the tokens are no longer valid (e.g. refresh token expired or revoked)
     */
    private fun refreshTokensIfExpired(authToken: OAuth2AuthenticationToken): Boolean {
        val authorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId(authToken.authorizedClientRegistrationId)
            .principal(authToken)
            .build()
        return try {
            authorizedClientManager.authorize(authorizeRequest) != null
        } catch (e: OAuth2AuthorizationException) {
            log.warn("[User] Token refresh failed for ${authToken.name}: ${e.message}")
            false
        } catch (e: OAuth2AuthenticationException) {
            log.warn("[User] Refreshed ID token rejected for ${authToken.name}: ${e.message}")
            false
        }
    }

    @GetMapping("/account")
    fun getCurrentUser(authToken: Principal?): User {
        // Currently, Principal == oauth2 auth token
        if (authToken !is AbstractAuthenticationToken) {
            throw IllegalArgumentException("AbstractAuthenticationToken expected, UserController can't handle ${authToken?.javaClass}!")
        }
        val attributes = userService.extractAttributesFromAuthToken(authToken)
        val user = userService.findUser(attributes)
        log.debug("[User] Account for principal $authToken: user $user")
        return user ?: throw AccountResourceException("User not be found for principal=$authToken")
    }

    /**
     * Principal names of all non-expired sessions (persisted by Spring Session JDBC)
     */
    @GetMapping("/${Constants.API_PATH_ADMIN}/session-users")
    fun getUsersFromSessionRegistry(): List<String> {
        return jdbcTemplate.queryForList(
            "SELECT DISTINCT principal_name FROM spring_session WHERE principal_name IS NOT NULL AND expiry_time > ?",
            String::class.java,
            System.currentTimeMillis()
        ).filterNotNull()
    }

    /**
     * Get list of user summaries
     * filter out root user which is considered internal (any maybe later users in 000000 range)
     */
    @GetMapping("/user-summaries")
    fun getUserSummaries(): List<UserSummary> {
        val items =
            userRepository.findAllUserSummaries().filter { user ->
                user.id.toString() != Constants.USER_SYSTEM
            }
        log.debug("[User] getUserSummaries() returned ${items.size} items")
        return items
    }

    /**
     * Request removal of user data (if authenticated)
     */
    @PostMapping("/remove-me")
    fun removeMe(authToken: Principal?): BooleanResult {
        log.info("[User] Received request to remove current user")
        if (authToken == null) {
            throw IllegalStateException("Removal can only be requested from authenticated context")
        }
        userService.removeMe(getCurrentUser(authToken))
        return BooleanResult(true)
    }
}
