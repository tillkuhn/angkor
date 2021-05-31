package net.timafe.angkor.security

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

/**
 * Auditing the Author of Changes With Spring Security
 *
 * Spring provides We provide @CreatedBy, @LastModifiedBy to capture the user who created or modified the entity
 * as well as @CreatedDate and @LastModifiedDate to capture the point in time this happened.
 *
 * Based on https://www.baeldung.com/database-auditing-jpa#4-auditing-the-author-of-changes-with-spring-security
 * and jhipster auditor aware implementation
 */
@Component
class SecurityAuditorAware(
    private val userService: UserService
) : AuditorAware<UUID> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Returns either the [UUID] of the currentUser
     * or the static [UUID] of the system user (default)
     */
    override fun getCurrentAuditor(): Optional<UUID> {
        val currentUser: User? = userService.getCurrentUser()
        log.trace("getCurrentAuditor for ${currentUser?.id}")
        return if (currentUser != null) Optional.of(currentUser.id!!) else Optional.of(UUID.fromString(Constants.USER_SYSTEM))
    }

}
