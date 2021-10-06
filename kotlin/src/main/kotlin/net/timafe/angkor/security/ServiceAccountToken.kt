package net.timafe.angkor.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import java.util.*

/**
 * https://www.petrikainulainen.net/programming/spring-framework/spring-from-the-trenches-invoking-a-secured-method-from-a-scheduled-job/
 */
class ServiceAccountToken(
    principal: String,
    val id: UUID
): UsernamePasswordAuthenticationToken(

    User.withUsername(principal)
        .password("")
        .authorities(AuthorityUtils.createAuthorityList("ROLE_INTERNAL"))
        .build(),
    "",

    )
