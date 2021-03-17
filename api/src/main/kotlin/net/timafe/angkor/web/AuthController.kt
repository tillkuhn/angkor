package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserSummary
import net.timafe.angkor.repo.UserRepository
import net.timafe.angkor.web.vm.BooleanResult
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.stream.Collectors

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping(Constants.API_LATEST)
class AuthController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val sessionRegistry: SessionRegistry
) {

    internal class AccountResourceException(message: String) : RuntimeException(message)

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/account")
    fun getCurrentUser(auth: Principal?): User {
        // Principal is oauth2 auth token
        if (auth !is OAuth2AuthenticationToken) {
            val msg =
                "User authenticated by AuthClass=${auth?.javaClass}, ${OAuth2AuthenticationToken::class.java} is supported"
            log.error(msg)
            throw IllegalArgumentException(msg)
        }
        val user = userService.findUser(auth.principal.attributes)
        log.debug("Account for principal $auth user $user")
        if (user != null) {
            return user
        } else {
            throw AccountResourceException("User could not be found or principal is $auth")
        }
    }

    /**
     * Can be used by frontend to check if the current user is authenticated
     * (Current SecurityContext != AnonymousAuthenticationToken)
     */
    @GetMapping("/authenticated")
    fun isAuthenticated(): BooleanResult {
        return BooleanResult(SecurityUtils.isAuthenticated())
    }

    @GetMapping("/${Constants.API_PATH_ADMIN}/session-users")
    fun getUsersFromSessionRegistry(): List<String?>? {
        return sessionRegistry.allPrincipals.stream()
            .filter { u -> sessionRegistry.getAllSessions(u, false).isNotEmpty() }
            .map { obj: Any -> obj.toString() }
            .collect(Collectors.toList())
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
        log.debug("getUserSummaries() returned ${items.size} items")
        return items
    }
}
