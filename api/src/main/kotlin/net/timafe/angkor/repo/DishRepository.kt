package net.timafe.angkor.repo

import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.enums.AuthScope
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface DishRepository : CrudRepository<Dish, UUID> {

    fun findByName(name: String): List<Dish>

    override fun findAll(): List<Dish>

    @Query("SELECT d FROM Dish d WHERE d.authScope IN (:authScopes)")
    fun findDishesByAuthScope(@Param("authScopes") authScopes: List<AuthScope>): List<Dish>

}
