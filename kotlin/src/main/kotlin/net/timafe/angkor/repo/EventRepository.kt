package net.timafe.angkor.repo

import net.timafe.angkor.domain.Event
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface EventRepository : CrudRepository<Event, UUID> {

    override fun findAll(): List<Event> // superclass only returns Iterable<T> f


    // @Query("SELECT e FROM Link l where l.mediaType = net.timafe.angkor.domain.enums.LinkMediaType.FEED ORDER BY l.createdAt")
    // https://www.baeldung.com/spring-data-sorting
    // https://www.baeldung.com/jpa-limit-query-results
    fun findFirst50ByOrderByTimeDesc(): List<Event>

    fun findFirst50ByTopicOrderByTimeDesc(topic: String): List<Event>

    @Query("SELECT COUNT(e) FROM Event e")
    fun itemCount(): Long
}
