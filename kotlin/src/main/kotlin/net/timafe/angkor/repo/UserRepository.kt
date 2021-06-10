package net.timafe.angkor.repo

import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserSummary
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*


interface UserRepository : CrudRepository<User, UUID> {
    companion object {
        const val USER_SUMMARIES_CACHE: String = "userSummariesCache"
        const val USERS_BY_LOGIN_CACHE: String = "usersByLoginCache"
    }

    // JPQL @Query with Named Parameters https://springframework.guru/spring-data-jpa-query/
    @Cacheable(cacheNames = [USERS_BY_LOGIN_CACHE])
    @Query("SELECT u FROM User u WHERE lower(u.login) = :login or lower(u.email) = :email or u.id = :id")
    fun findByLoginOrEmailOrId(
        @Param("login") login: String?,
        @Param("email") email: String?,
        @Param("id") id: UUID?
    ): List<User>

    override fun findAll(): List<User> // return list instead of iterable

    @Query("SELECT new net.timafe.angkor.domain.dto.UserSummary(u.id,u.name,u.emoji) FROM User u")
    @Cacheable(cacheNames = [USER_SUMMARIES_CACHE])
    fun findAllUserSummaries(): List<UserSummary>

}
