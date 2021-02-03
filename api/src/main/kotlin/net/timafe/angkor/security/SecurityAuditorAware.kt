package net.timafe.angkor.security

import net.timafe.angkor.config.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

// https://www.baeldung.com/database-auditing-jpa#4-auditing-the-author-of-changes-with-spring-security
// And jhipster auditor aware
@Component
class SecurityAuditorAware(
    private val authService: AuthService
): AuditorAware<UUID> {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun getCurrentAuditor(): Optional<UUID> {
        val currentUser = authService.currentUser
        log.trace("getCurrentAuditor for ${authService.currentUser?.id}")
        return if (currentUser != null) Optional.of(currentUser.id!!) else Optional.of(UUID.fromString(Constants.USER_SYSTEM))
    }


}
