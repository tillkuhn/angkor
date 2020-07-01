package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.servlet.http.HttpServletRequest

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping(Constants.API_SECURE)
class AccountResource(private val userService: UserService) {

    internal class AccountResourceException(message: String) : RuntimeException(message)

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * `GET  /authenticate` : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    fun isAuthenticated(request: HttpServletRequest): String? {
        log.debug("REST request to check if remoteUser=${request.remoteUser} is authenticated")
        return request.remoteUser
    }

    /**
     * `GET  /account` : get the current user.
     *
     * @param principal the current user; resolves to `null` if not authenticated.
     * @return the current user.
     * @throws AccountResourceException `500 (Internal Server Error)` if the user couldn't be returned.
     */
    @GetMapping("/account")
    fun getAccount(principal: Principal?): User =
            if (principal is AbstractAuthenticationToken) {
                userService.getUserFromAuthentication(principal)
            } else {
                throw AccountResourceException("User could not be found")
            }
}
