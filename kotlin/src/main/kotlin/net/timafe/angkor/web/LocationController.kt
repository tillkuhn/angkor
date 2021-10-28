package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Location
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Order
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.validation.Valid
import kotlin.reflect.KClass


/**
 * Polymorphic location queries
 */
// Vlad: How to query by entity type using JPA Criteria API
// https://vladmihalcea.com/query-entity-type-jpa-criteria-api/
// Baeldung: https://www.baeldung.com/hibernate-criteria-queries
// "All JPQL queries are polymorphic." (so we also get subclass fields)
// https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/jpql-polymorphic-queries.html
// Dynamic, typesafe queries in JPA 2.0
// How the Criteria API builds dynamic queries and reduces run-time failures
// https://developer.ibm.com/articles/j-typesafejpa/
@RestController
@RequestMapping(Constants.API_LATEST+"/locations")
class LocationController(
    private val entityManager: EntityManager,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Search all items, delegates to post search with empty request
     */
    @GetMapping("search/")
    fun searchAll(): List<Location> = search(SearchRequest())

    // Convenient method to filter on tours (temporary)
    @GetMapping("tours") // locations/tours
    fun searchTours(): List<Location> = search(SearchRequest(entityTypes = mutableListOf(EntityType.TOUR)))

    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    fun search(@Valid @RequestBody search: SearchRequest): List<Location> {
        val builder = entityManager.criteriaBuilder
        val query: CriteriaQuery<Location> = builder.createQuery(Location::class.java)

        val root: Root<Location> = query.from(Location::class.java)
        val andPredicates = mutableListOf<Predicate>()

        // the main case-insensitive "like" query against name and potentially other freetext fields
        if (search.query.isNotEmpty()) {
            // use lower: https://stackoverflow.com/q/43089561/4292075
            andPredicates.add(builder.like(builder.lower(root.get("name")), "%${search.query.lowercase()}%"))
        }

        // this is cool we can dynamically filter the Location entities by the subclass type,
        // by using the type method of the Path Criteria API class (see Vlads tutorial)
        if (search.entityTypes.isNotEmpty()) {
            // we need to perform some magic to get the javaClasses (required for Criteria API)
            // from the EntityType Enum List in SearchRequest
            val typeClasses = mutableListOf<Class<out Location>>()
            for (entityType in search.entityTypes) {
                val subclass: KClass<out Location> = entityTypeToClass(entityType)
                typeClasses.add(subclass.java)
            }
            val typePredicate = root.type().`in`(*typeClasses.toTypedArray())
            andPredicates.add(typePredicate)
        }

        // also filter by authscope(s), should later be dynamically based on SecurityContext
        andPredicates.add(root.get<Any>("authScope").`in`(AuthScope.PUBLIC))

        // and here comes the where clause (if there's at least one predicate)
        if (andPredicates.isNotEmpty()) {
            query.where(
                builder.and(*andPredicates.toTypedArray())
            )
        }

        // order in the court (yes we should have sortOrder per field, not a single prop for all sort fields)
        if (search.sortProperties.isNotEmpty()) {
            val orders = mutableListOf<Order>()
            for (sortProp in search.sortProperties) {
                if (search.sortDirection == Sort.Direction.DESC) {
                    orders.add(builder.desc(root.get<Any>(sortProp)))
                } else {
                    orders.add(builder.desc(root.get<Any>(sortProp)))
                }
            }
            query.orderBy(*orders.toTypedArray())
        }

        // off we go
        val items = entityManager.createQuery(query).resultList
        log.debug("[Location] Search '$search': ${items.size} results")
        return items
    }

    // Helper to convert from EntityType enum to Kotlin (and later Java) class
    private fun entityTypeToClass(entityType: EntityType): KClass<out Location> {
        return when (entityType) {
            EntityType.TOUR -> Tour::class
            EntityType.VIDEO -> Video::class
            // More to come
            else -> throw IllegalArgumentException()
        }
    }

}
