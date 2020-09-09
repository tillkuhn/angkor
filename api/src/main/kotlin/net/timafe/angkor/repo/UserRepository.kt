package net.timafe.angkor.repo

import net.timafe.angkor.domain.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.awt.print.Book




interface UserRepository : CrudRepository<User, String> {

    // JPQL @Query with Named Parameters https://springframework.guru/spring-data-jpa-query/
    @Query("SELECT u FROM User u WHERE u.login = :login or u.email= :email or u.id = :id")
    fun findByLoginOrEmailOrId(@Param("login") login: String?,
                               @Param("email") email: String?,
                               @Param("id") id: String?): List<User>

    fun findByLogin(login: String): List<User>
    fun findByEmail(email: String): List<User>
    override fun findAll(): List<User>
}
