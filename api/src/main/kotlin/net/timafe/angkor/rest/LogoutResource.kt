package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 * REST controller for managing global OIDC logout.
 */
@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION)
class LogoutResource(registrations: ClientRegistrationRepository) {
    private val registration: ClientRegistration = registrations.findByRegistrationId("cognito")
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * `POST  /api/logout` : logout the current user.
     *
     * @param request the [HttpServletRequest].
     * @param idToken the ID token.
     * @return the [ResponseEntity] with status `200 (OK)` and a body with a global logout URL and ID token.
     */
    @PostMapping("/logout")
    fun logout(
            request: HttpServletRequest,
            @AuthenticationPrincipal(expression = "idToken") idToken: OidcIdToken?
    ): ResponseEntity<*> {
        log.info("Logging out current user")
        val logoutUrl = registration.providerDetails.configurationMetadata["end_session_endpoint"].toString()

        val logoutDetails = mutableMapOf(
                "logoutUrl" to logoutUrl,
                "idToken" to idToken?.tokenValue
        )
        request.session.invalidate()
        return ResponseEntity.ok().body(logoutDetails)
    }
}
