package net.timafe.angkor.repo

import net.timafe.angkor.domain.Dish
import org.springframework.data.repository.CrudRepository
import java.util.*

interface DishRepository : CrudRepository<Dish, UUID> {

    fun findByName(name: String): List<Dish>

    override fun findAll(): List<Dish>

}
