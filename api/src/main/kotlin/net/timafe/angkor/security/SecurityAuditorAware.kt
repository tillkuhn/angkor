package net.timafe.angkor.security

import net.timafe.angkor.config.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

/**
 * Auditing the Author of Changes With Spring Security
 * Based on https://www.baeldung.com/database-auditing-jpa#4-auditing-the-author-of-changes-with-spring-security
 * and jhipster auditor aware
 */
@Component
class SecurityAuditorAware(
    private val authService: AuthService
) : AuditorAware<UUID> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Returns either the UUID of the currentUser or the static UUID of the system user (default)
     */
    override fun getCurrentAuditor(): Optional<UUID> {
        val currentUser = authService.currentUser
        log.trace("getCurrentAuditor for ${authService.currentUser?.id}")
        return if (currentUser != null) Optional.of(currentUser.id!!) else Optional.of(UUID.fromString(Constants.USER_SYSTEM))
    }


}
