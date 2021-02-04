package net.timafe.angkor.service

import java.util.*

interface EntityService<ET,EST,ID> {

    fun save(item: ET): ET

    fun findAll(): List<ET>

    fun findOne(id: ID): Optional<ET>

    fun delete(id: ID)

    fun search(search: String): List<EST>
}
