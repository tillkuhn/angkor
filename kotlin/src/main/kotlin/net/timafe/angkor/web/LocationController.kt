package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Location
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.LocationRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root
import javax.validation.Valid
import kotlin.reflect.KClass


@RestController
@RequestMapping(Constants.API_LATEST)
class LocationController(
    private val entityManager: EntityManager,
    private val repo: LocationRepository,
) {

    @GetMapping("/locations")
    fun findAll(): List<Location> = repo.findAll().toList()

    // Vlad: How to query by entity type using JPA Criteria API
    // https://vladmihalcea.com/query-entity-type-jpa-criteria-api/
    // Baeldung: https://www.baeldung.com/hibernate-criteria-queries
    // "All JPQL queries are polymorphic." (so we also get subclass fields)
    // https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/jpql-polymorphic-queries.html
    @GetMapping("/locations/tours")
    fun searchTours(): List<Location> = search(SearchRequest(entityTypes = mutableListOf(EntityType.TOUR)))


    fun search(@Valid @RequestBody search: SearchRequest): List<Location> {
        val builder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Location> = builder.createQuery(Location::class.java)

        val root: Root<Location> = criteria.from(Location::class.java)
        // https://stackoverflow.com/q/43089561/4292075
        val searchPredicate = builder.like(builder.lower(root.get("name")),"%${search.query}%")

        if (search.entityTypes.isNotEmpty()) {
            // val typePredicates = mutableListOf<Predicate>()
            val typeClasses = mutableListOf<Class<out Location> >()
            for (entityType in search.entityTypes)  {
                val subclass: KClass<out Location> = entityTypeToClass(entityType)
                typeClasses.add(subclass.java)
                // typePredicates.add(builder.equal(root.type(), subclass.java))
            }
            // val typePredicate = builder.or(*typePredicates.toTypedArray()) // must be or
            val typePredicate = root.type().`in`(*typeClasses.toTypedArray())
            criteria.where(
                builder.and(searchPredicate, typePredicate) // and-combined with search
            )
        } else {
            criteria.where(searchPredicate) // only search
        }
        // builder.equal(root.get<Any>("owner"), "Vlad"),

        criteria.orderBy(
            //criteria.asc(root.get("itemName")),
            builder.desc(root.get<Any>("updatedAt"))
        )
        return entityManager
            .createQuery(criteria)
            .resultList
    }

    private fun entityTypeToClass(entityType: EntityType): KClass<out Location> {
        return when (entityType) {
            EntityType.TOUR -> Tour::class
            EntityType.VIDEO -> Video::class
            // More to come
            else -> throw IllegalArgumentException()
        }
    }

}
