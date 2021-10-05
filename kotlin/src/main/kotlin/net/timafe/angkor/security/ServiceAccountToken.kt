package net.timafe.angkor.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User

/**
 * https://www.petrikainulainen.net/programming/spring-framework/spring-from-the-trenches-invoking-a-secured-method-from-a-scheduled-job/
 */
class ServiceAccountToken(
    principal: String
): UsernamePasswordAuthenticationToken(
    User.withUsername(principal)
        .password("")
        .authorities(AuthorityUtils.createAuthorityList("ROLE_INTERNAL"))
        .build(),
    "",
) {
    constructor(callerClass: Class<*>) : this(callerClass.simpleName.lowercase())
}
