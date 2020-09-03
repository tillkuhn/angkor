package net.timafe.angkor.repo

import net.timafe.angkor.domain.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, String> {

    fun findByLogin(login: String): List<User>
    fun findByEmail(email: String): List<User>
    override fun findAll(): List<User>
}
