package net.timafe.angkor.service

import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserSummary
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.UserRepository
import net.timafe.angkor.security.SecurityUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * Check https://github.com/rajithd/spring-boot-oauth2/blob/master/src/main/java/com/rd/security/UserDetailsService.java
 *
 */
@Service
class UserService(
    private val userRepository: UserRepository
) : EntityService<User, UserSummary, UUID>(userRepository) {

    @Transactional(readOnly = true)
    fun findUser(attributes: Map<String, Any>): User? {
        val sub = attributes["sub"] as String
        val cognitoUsername = attributes[SecurityUtils.COGNITO_USERNAME_KEY] as String?
        val login = cognitoUsername ?: sub
        val email = attributes["email"] as String?
        val id: UUID? = SecurityUtils.extractUUIDfromSubject(sub)
        val users = userRepository.findByLoginOrEmailOrId(login.toLowerCase(), email?.toLowerCase(), id)
        if (users.size > 1) {
            throw IllegalStateException("Expected max 1 user for $login, $email, $id - but found ${users.size}")
        }
        return if (users.isNotEmpty()) users[0] else null
    }

    @Transactional
    @CacheEvict(cacheNames = [UserRepository.USERS_BY_LOGIN_CACHE],allEntries = true)
    fun createUser(attributes: Map<String, Any>) {
        val sub = attributes["sub"] as String
        val id = SecurityUtils.extractUUIDfromSubject(sub) ?: UUID.randomUUID()
        val cognitoUsername = attributes[SecurityUtils.COGNITO_USERNAME_KEY] as String?
        val login = cognitoUsername ?: sub
        val name = attributes["name"] ?: login
        val roles = SecurityUtils.getRolesFromClaims(attributes)
        log.info("[${entityType()}] Creating new local db user $id (sub=$sub)")
        this.save(
            User(
                id = id,
                login = login,
                email = attributes["email"] as String?,
                firstName = attributes["given_name"] as String?,
                lastName = attributes["family_name"] as String?,
                name = name as String?,
                lastLogin = LocalDateTime.now(), roles = ArrayList(roles)
            )
        )
    }

    @CacheEvict(cacheNames = [UserRepository.USERS_BY_LOGIN_CACHE],allEntries = true)
    override fun save(item: User): User {
        return super.save(item)
    }

    fun getCurrentUser(): User? {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth !is OAuth2AuthenticationToken) {
            val msg = "Unsupported AuthClass=${auth?.javaClass}, expected ${OAuth2LoginAuthenticationToken::class.java}"
            log.warn(msg)
            return null
        }
        return findUser(auth.principal.attributes)!!
    }

    override fun entityType(): EntityType {
        return EntityType.USER
    }

}


