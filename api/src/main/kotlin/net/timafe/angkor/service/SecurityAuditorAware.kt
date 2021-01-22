package net.timafe.angkor.service

import net.timafe.angkor.config.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*

// https://www.baeldung.com/database-auditing-jpa#4-auditing-the-author-of-changes-with-spring-security
// And jhipster auditor aware
@Component
class SecurityAuditorAware(
    private val authService: AuthService
): AuditorAware<String> {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun getCurrentAuditor(): Optional<String> {
        val currentUser = authService.currentUser
        log.trace("getCurrentAuditor for ${authService.currentUser?.id}")
        return if (currentUser != null) Optional.of(currentUser.id!!) else Optional.of(Constants.USER_SYSTEM)
    }


}
