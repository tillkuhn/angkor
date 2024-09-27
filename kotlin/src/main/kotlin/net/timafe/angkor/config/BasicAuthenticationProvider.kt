package net.timafe.angkor.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component


/**
 * Custom Auth Provider inspired by https://howtodoinjava.com/spring-security/custom-authentication-providers/
 * to handle user cases for basic auth, e.g. protected prometheus actuator endpoint
 *
 * with exception: https://www.javadevjournal.com/spring-security/spring-security-custom-authentication-provider/
 */
@Component
class BasicAuthenticationProvider(
    @Value("\${app.metrics.basic-auth-user}")  private val metricsUser: String,
    @Value("\${app.metrics.basic-auth-password}")  private val metricsToken: String
) : AuthenticationProvider {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * isValidUser currently only supports a single use case (prometheus basic auth for actuator metrics)
     * but others may follow
     */
    private fun isValidUser(username: String, password: String): UserDetails? {
        if (username.equals(metricsUser, ignoreCase = true) && password == metricsToken) {
            val user: UserDetails = User
                .withUsername(username)
                .password("*".repeat(password.length))
                .authorities(SimpleGrantedAuthority("METRICS_VIEWER"))
                .build()

            return user
        } else {
            log.warn("Invalid basic auth credentials for principal=${username} pw-len=${password.length}")
        }
        return null
    }

    override fun authenticate(authentication: Authentication): Authentication {
        val username: String = authentication.name
        val password: String = authentication.credentials.toString()

        val userDetails = isValidUser(username, password)

        if (userDetails != null) {
            return UsernamePasswordAuthenticationToken(
                username,
                password,
                userDetails.authorities
            )
        } else {
            throw BadCredentialsException("Incorrect user credentials for $username!")
        }
    }

    override fun supports(authenticationType: Class<*>): Boolean {
        return (authenticationType
                == UsernamePasswordAuthenticationToken::class.java)
    }
}
