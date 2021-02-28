package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserSummary
import net.timafe.angkor.repo.UserRepository
import net.timafe.angkor.rest.vm.BooleanResult
import net.timafe.angkor.security.AuthService
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.LoggerFactory
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
class AuthController(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val sessionRegistry: SessionRegistry
) {

    internal class AccountResourceException(message: String) : RuntimeException(message)

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/account")
    fun getCurrentUser(principal: Principal?): User {
        val user = authService.currentUser
        log.debug("Account for principal $principal user $user")
        if (user != null) {
            return user
        } else {
            throw AccountResourceException("User could not be found or principal is $principal")
        }
        /*
        if (principal != null && principal is OAuth2AuthenticationToken) {
            return authService.getUserFromAuthentication(principal)
        } else {
            // return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
         */
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

    @GetMapping("/user-summaries")
    fun getUserSummaries(): List<UserSummary> {
        val items =
            userRepository.findAllUserSummaries().filter {
                    user -> !user.id.toString().equals(Constants.USER_SYSTEM)
            } // filter out root user (any maybe later users in 000000 range)   
        log.debug("getUserSummaries() returned ${items.size} items")
        //return items.filter { it.getCoordinates().size > 1 }
        return items
    }
}
