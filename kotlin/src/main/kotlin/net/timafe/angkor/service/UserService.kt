package net.timafe.angkor.service

import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserSummary
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.UserRepository
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.security.ServiceAccountToken
import org.springframework.cache.annotation.CacheEvict
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.*

/**
 * Manage [User]
 * Check https://github.com/rajithd/spring-boot-oauth2/blob/master/src/main/java/com/rd/security/UserDetailsService.java
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val cacheService: CacheService,
    private val mailService: MailService,
) : AbstractEntityService<User, UserSummary, UUID>(userRepository) {

    @Transactional
    @CacheEvict(
        cacheNames = [UserRepository.USERS_BY_LOGIN_CACHE, UserRepository.USER_SUMMARIES_CACHE],
        allEntries = true
    )
    fun createUser(attributes: Map<String, Any>) {
        val subject = attributes[SecurityUtils.JWT_SUBJECT_KEY] as String
        val id = SecurityUtils.safeConvertToUUID(subject) ?: UUID.randomUUID()
        val cognitoUsername = attributes[SecurityUtils.COGNITO_USERNAME_KEY] as String?
        val login = cognitoUsername ?: subject
        val name = attributes["name"] ?: login
        val roles = SecurityUtils.getRolesFromAttributes(attributes)
        log.info("[${entityType()}] Create new local db user $id (sub=$subject)")
        this.save(
            User(
                id = id,
                login = login,
                email = attributes["email"] as String?,
                firstName = attributes["given_name"] as String?,
                lastName = attributes["family_name"] as String?,
                name = name as String?,
                lastLogin = ZonedDateTime.now(), roles = ArrayList(roles)
            )
        )
    }

    @CacheEvict(cacheNames = [UserRepository.USERS_BY_LOGIN_CACHE], allEntries = true)
    override fun save(item: User): User {
        return super.save(item)
    }

    @Transactional(readOnly = true) // this is important (issue with IT tests)
    fun findUser(attributes: Map<String, Any>): User? {
        val sub = attributes[SecurityUtils.JWT_SUBJECT_KEY] as String
        val cognitoUsername = attributes[SecurityUtils.COGNITO_USERNAME_KEY] as String?
        val login = cognitoUsername ?: sub
        val email = attributes["email"] as String?
        val id: UUID? = SecurityUtils.safeConvertToUUID(sub)
        val users = userRepository.findByLoginOrEmailOrId(login.lowercase(), email?.lowercase(), id)
        if (users.size > 1) {
            throw IllegalStateException("Expected max 1 user for $login, $email, $id - but found ${users.size}")
        }
        return if (users.isNotEmpty()) users[0] else null
    }


    /**
     * Gets the current User (DB Entity) based on information provided by the SecurityContext's Authentication
     */
    @Transactional(readOnly = true)
    fun getCurrentUser(): User? {
        val auth = SecurityContextHolder.getContext().authentication
        // Note: For MockTestUser etc. we also support UsernamePasswordAuthenticationToken to extract subject from sub
        // When invoked from a @Scheduled job, we get a warning here which we should compensate.
        // TODO e.g. like this https://www.petrikainulainen.net/programming/spring-framework/spring-from-the-trenches-invoking-a-secured-method-from-a-scheduled-job/
        // or https://stackoverflow.com/questions/63346374/how-to-configure-graceful-shutdown-using-delegatingsecuritycontextscheduledexecu
        if (auth !is AbstractAuthenticationToken) {
            log.warn("${super.logPrefix()} Unsupported AuthClass=${auth?.javaClass}, expected ${OAuth2LoginAuthenticationToken::class.java}")
            return null
        }
        val attributes = extractAttributesFromAuthToken(auth)
        return findUser(attributes)
    }

    fun getServiceAccountToken(callerClass: Class<*>): ServiceAccountToken {
        val login = callerClass.simpleName.lowercase()
        val users = userRepository.findByLoginOrEmailOrId(login,null,null)
        if (users.size != 1) {
            throw IllegalStateException("Expected max 1 service account user for $login - but found ${users.size}")
        }
        return ServiceAccountToken(login,users[0].id!!)
    }

    /**
     * Extracts the principals / tokens attribute map, currently supports instances of
     * OAuth2AuthenticationToken and OAuth2LoginAuthenticationToken
     */
    fun extractAttributesFromAuthToken(authToken: AbstractAuthenticationToken): Map<String, Any> =
        when (authToken) {
            // For OAuth2 Tokens, the Principal is of type OAuth2User
            is OAuth2AuthenticationToken -> authToken.principal.attributes
            is OAuth2LoginAuthenticationToken -> authToken.principal.attributes
            // no Attributes since principal is just an Object of type ...userDetails.User (with username / password)
            // but we also have authorities
            is UsernamePasswordAuthenticationToken -> getAttributesForUsernamePasswordAuth(authToken)
            // JwtAuthenticationToken not yet supported, would use authToken.tokenAttributes
            else -> throw IllegalArgumentException("Unsupported auth token, UserService can't handle ${authToken.javaClass}!")
        }

    /**
     * Request removal of user data
     */
    fun removeMe(user: User) {
        // let - avoid ‘property’ is a mutable property that could have been changed by this time issues
        // https://medium.com/android-news/lets-talk-about-kotlin-s-let-extension-function-5911213cf8b9
        // let captures the value T for thread-safe reading
        // If the value is an optional, you probably want to unwrap it first with ?. so that your T is not an optional
        // ere we use the elvis operator ?: to guarantee we run one of the conditional branches. If property exists,
        // then we can capture and use its value, if the value is null we can ensure we show an error.
        user.email?.let {
            mailService.prepareAndSend(it, "Request for user deletion received"
                , "<p>Dear User,<br /><br />We received your request for <i>user data deletion</i> and will process it asap. Thanks for using our services! </p><p>Your Admin Team</p>")
        } ?: throw IllegalArgumentException("User ${user.login} has no mail address")
    }

    fun extractIdTokenFromAuthToken(authToken: AbstractAuthenticationToken): String =
        when (val prince = authToken.principal) {
            is DefaultOidcUser -> prince.idToken.tokenValue
            else -> throw IllegalArgumentException("Unsupported principal class, UserService can't handle ${prince.javaClass}!")
        }


    private fun getAttributesForUsernamePasswordAuth(authToken: UsernamePasswordAuthenticationToken): Map<String, Any> {
        val prince = authToken.principal
        return if (prince is org.springframework.security.core.userdetails.User) {
            mapOf(SecurityUtils.JWT_SUBJECT_KEY to prince.username)
        } else {
            mapOf()
        }
    }

    // Currently, we use  @CacheEvict annotation, but this may be useful if we need to evict from within the service
    fun clearCaches() {
        cacheService.clearCache(UserRepository.USER_SUMMARIES_CACHE)
        cacheService.clearCache(UserRepository.USERS_BY_LOGIN_CACHE)
        // From khipster
        // cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)?.evict(user.login!!)
    }

    // Required by EntityService Superclass
    override fun entityType(): EntityType {
        return EntityType.User
    }

}


