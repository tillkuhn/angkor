package net.timafe.angkor.security

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
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
        // shortcut for service account tokens where we know the uuid
        val auth = SecurityContextHolder.getContext().authentication
        if (auth is ServiceAccountToken) {
            return Optional.of(auth.id)
        }
        val currentUser: User? = userService.getCurrentUser()
        log.trace("getCurrentAuditor for ${currentUser?.id}")
        return if (currentUser != null) Optional.of(currentUser.id!!) else Optional.of(UUID.fromString(Constants.USER_SYSTEM))
    }


    // Use a custom DateTimeProvider b/c of: Invalid date type exception when using JPA Auditor, SpringBoot
    // Ref.: https://github.com/spring-projects/spring-boot/issues/10743
    @Bean(name = ["auditingDateTimeProvider"])
    fun dateTimeProvider(): DateTimeProvider? {
        return DateTimeProvider { Optional.of(OffsetDateTime.now()) }
    }
}
