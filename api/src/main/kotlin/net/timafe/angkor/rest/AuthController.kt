package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.UserDTO
import net.timafe.angkor.rest.vm.BooleanResult
import net.timafe.angkor.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*
import java.security.Principal

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION)
class AuthController(private val authService: AuthService) {

    internal class AccountResourceException(message: String) : RuntimeException(message)

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/account")
    fun getAccount(principal: Principal?) : UserDTO {
        log.debug("Account for principal $principal")
        if (principal != null && principal is OAuth2AuthenticationToken) {
            return authService.getUserFromAuthentication(principal)
        } else {
            // return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            throw AccountResourceException("User could not be found or principal is $principal")
        }
     }

    @GetMapping("/authenticated")
    fun isAuthenticated(principal: Principal?) : BooleanResult {
        log.debug("isAuthenticated for principal $principal")
        return BooleanResult(principal != null)
        // return ResponseEntity(principal != null,HttpStatus.OK);
    }


    /**
     * `GET  /authenticate` : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    /*
    @GetMapping("/authenticate")
    fun isAuthenticated(request: HttpServletRequest): String? {
        log.debug("REST request to check if remoteUser=${request.remoteUser} is authenticated")
        return request.remoteUser
    }
    */

    /**
     * `GET  /account` : get the current user.
     *
     * @param principal the current user; resolves to `null` if not authenticated.
     * @return the current user.
     * @throws AccountResourceException `500 (Internal Server Error)` if the user couldn't be returned.
     */
    /*
    @GetMapping("/account")
    fun getAccount(principal: Principal?): User =
            if (principal is AbstractAuthenticationToken) {
                userService.getUserFromAuthentication(principal)
            } else {
                throw AccountResourceException("User could not be found")
            }

     */
}
