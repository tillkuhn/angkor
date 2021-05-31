package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserSummary
import net.timafe.angkor.repo.UserRepository
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.UserService
import net.timafe.angkor.web.vm.AuthenticationVM
import net.timafe.angkor.web.vm.BooleanResult
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.session.SessionRegistry
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
class UserController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val sessionRegistry: SessionRegistry
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
    fun getAuthentication(authToken: Principal?): AuthenticationVM {
        if (SecurityUtils.isAuthenticated()) {
            if (authToken !is AbstractAuthenticationToken) {
                throw IllegalArgumentException("AbstractAuthenticationToken expected, UserController can't handle ${authToken?.javaClass}!")
            }
            return AuthenticationVM(authenticated = true,
                        user = getCurrentUser(authToken),
                        idToken = userService.extractIdTokenFromAuthToken(authToken))
        } else {
            return AuthenticationVM(authenticated = false, user = null, idToken = null)
        }
    }

    @GetMapping("/account")
    fun getCurrentUser(authToken: Principal?): User {
        // Currently Principal == oauth2 auth token
        if (authToken !is AbstractAuthenticationToken) {
            throw IllegalArgumentException("AbstractAuthenticationToken expected, UserController can't handle ${authToken?.javaClass}!")
        }
        val attributes = userService.extractAttributesFromAuthToken(authToken)
        val user = userService.findUser(attributes)
        log.debug("User Account for principal $authToken: user $user")
        return user ?: throw AccountResourceException("User not be found for principal=$authToken")
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
                user.id?.toString() != Constants.USER_SYSTEM
            }
        log.debug("${userService.logPrefix()} getUserSummaries() returned ${items.size} items")
        return items
    }
}
