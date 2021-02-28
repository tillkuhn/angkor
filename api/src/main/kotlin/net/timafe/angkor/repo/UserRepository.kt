package net.timafe.angkor.repo

import net.timafe.angkor.domain.User
import net.timafe.angkor.domain.dto.UserSummary
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*


interface UserRepository : CrudRepository<User, String> {
    companion object {
        const val USER_SUMMARIES_CACHE: String = "userSummariesCache"
    }

    // JPQL @Query with Named Parameters https://springframework.guru/spring-data-jpa-query/
    @Query("SELECT u FROM User u WHERE u.login = :login or u.email= :email or u.id = :id")
    fun findByLoginOrEmailOrId(
        @Param("login") login: String?,
        @Param("email") email: String?,
        @Param("id") id: UUID?
    ): List<User>

    override fun findAll(): List<User>

    @Query("SELECT new net.timafe.angkor.domain.dto.UserSummary(u.id,u.name,u.emoji) FROM User u")
    @Cacheable(cacheNames = [USER_SUMMARIES_CACHE])
    fun findAllUserSummaries(): List<UserSummary>

    // fun findByLogin(login: String): List<User>
    // fun findByEmail(email: String): List<User>

}
