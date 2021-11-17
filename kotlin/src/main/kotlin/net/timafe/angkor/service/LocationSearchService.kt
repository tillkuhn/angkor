package net.timafe.angkor.service

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.*
import net.timafe.angkor.domain.dto.LocationSummary
import net.timafe.angkor.domain.dto.MapLocation
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.repo.LocationRepository
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import javax.persistence.EntityManager
import javax.persistence.criteria.Order
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Selection
import kotlin.reflect.KClass

/**
 * LocationService, mainly responsible for dealing
 * with Polymorphic location queries
 */
// Vlad: How to query by entity type using JPA Criteria API
// https://vladmihalcea.com/query-entity-type-jpa-criteria-api/
//
// Baeldung: https://www.baeldung.com/hibernate-criteria-queries
// "All JPQL queries are polymorphic." (so we also get subclass fields)
// https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/jpql-polymorphic-queries.html
//
// Dynamic, typesafe queries in JPA 2.0
// How the Criteria API builds dynamic queries and reduces run-time failures
// https://developer.ibm.com/articles/j-typesafejpa/
//
// How to filter a PostgreSQL array column with the JPA Criteria API?
// https://stackoverflow.com/a/24695695/4292075
//
// Vlad CriteriaAPITest with lots of useful code
// https://github.com/vladmihalcea/high-performance-java-persistence/blob/master/core/src/test/java/com/vladmihalcea/book/hpjp/hibernate/fetching/CriteriaAPITest.java
//
// JPA & Criteria API - Select only specific columns
// https://coderedirect.com/questions/99538/jpa-criteria-api-select-only-specific-columns
// https://www.baeldung.com/jpa-hibernate-projections#hibernatesinglecolumn
//
// How do DTO projections work with JPA and Hibernate
// https://thorben-janssen.com/dto-projections/
//
// Making JPA Criteria API less awkward with Kotlin
// http://lifeinide.com/post/2021-04-29-making-jpa-criteria-api-less-awkward-with-kotlin/
@Service
class LocationSearchService(
    private val entityManager: EntityManager,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Search by SearchRequest,
     *
     * @return  List of LocationSummary DTOs
     * Mainly used by LocationSearchController to delegate searches triggered by the UI
     */
    fun search(search: SearchRequest): List<LocationSummary> {
        val cArgs = listOf(
            "areaCode", "authScope", "id", "imageUrl", "name", "primaryUrl", "updatedAt",
            "updatedBy", "coordinates", "tags", "type"
        )
        return search(search, LocationSummary::class, cArgs)
    }

    /**
     * Search by SearchRequest, return  location summaries
     * Mainly used by LocationSearchController to delegate searches triggered by the UI
     */
    fun searchPOIs(search: SearchRequest): List<MapLocation> {
        val constructorArgs = listOf("areaCode", "coordinates", "id", "imageUrl", "name", "type")
        return search(search, MapLocation::class, constructorArgs)
    }

    /**
     * Search by flexible POST SearchRequest query,
     * @param search Search based on which the WHERE query is constructed
     * @param resultClass the target class, can be the same as entityCLass or DTO Projection
     * @param constructorArgs  list of matching constructor args for the resultClass (order matters!)
     * @return A list of resultClass objects
     */
    private fun <T : Any> search(
        search: SearchRequest,
        resultClass: KClass<T>,
        constructorArgs: List<String>
    ): List<T> {

        // entityClass for query root (i.e. the class to "select from")
        val entityClass = Location::class

        // Start with Creating a criteria query object
        val cBuilder = entityManager.criteriaBuilder
        val cQuery = cBuilder.createQuery(resultClass.java) // : CriteriaQuery<ResultClass>

        // // Define FROM clause based on entityClass (i.e. Location)
        val root = cQuery.from(entityClass.java)

        // Define DTO projection if result class is different (experimental, see links on class level)
        if (resultClass != entityClass) {
            // build custom select section based on constructor args of target class
            val selections = mutableListOf<Selection<*>>()
            for (coArg in constructorArgs) {
                when (coArg) {
                    // this translates into the Java Subclass (e.g. net.timafe.angkor.domain.Place)
                    "type" -> selections.add(root.type())
                    // this is the default selection case
                    else -> selections.add(root.get<Any>(coArg))
                    // you can also add functions like lower, concat etc.
                    // cBuilder.concat(author.get(Author_.firstName), ' ', author.get(Author_.lastName))
                }
            }
            cQuery.select(
                cBuilder.construct(
                    resultClass.java,
                    *selections.toTypedArray()
                )
            )
        }

        // the main case-insensitive "like" query against name and potentially other freetext fields
        val andPredicates = mutableListOf<Predicate>()
        if (search.query.isNotEmpty()) {
            // use lower: https://stackoverflow.com/q/43089561/4292075
            val queryPredicates = mutableListOf<Predicate>()
            queryPredicates.add(cBuilder.like(cBuilder.lower(root.get("name")), "%${search.query.lowercase()}%"))
            val tagArray = cBuilder.function("text_array", String::class.java, root.get<Any>("tags"))
            queryPredicates.add(cBuilder.like(tagArray, "%${search.query.lowercase()}%"))
            // all potential query fields make up a single or query which we add to the "master" and list
            andPredicates.add(cBuilder.or(*queryPredicates.toTypedArray()))
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

        // also filter by authscope(s) if the entityClass implements AuthScoped
        if (AuthScoped::class.java.isAssignableFrom(entityClass.java)) {
            val authScopes = SecurityUtils.allowedAuthScopes()
            andPredicates.add(root.get<Any>("authScope").`in`(*authScopes.toTypedArray()))
        }

        // Construct where clause (if there's at least one AND predicate)
        if (andPredicates.isNotEmpty()) {
            cQuery.where(
                cBuilder.and(*andPredicates.toTypedArray())
            )
        }

        // order in the court (yes we should have sortOrder per field, not a single prop for all sort fields)
        if (search.sortProperties.isNotEmpty()) {
            val orders = mutableListOf<Order>()
            for (sortProp in search.sortProperties) {
                val propPath = root.get<Any>(sortProp)
                if (search.sortDirection == Sort.Direction.DESC) {
                    orders.add(cBuilder.desc(propPath))
                } else {
                    orders.add(cBuilder.asc(propPath))
                }
            }
            cQuery.orderBy(*orders.toTypedArray())
        }

        // off we go
        val typedQuery = entityManager.createQuery(cQuery)
        val maxRes = Constants.JPA_DEFAULT_RESULT_LIMIT / 2 // keep it smaller for evaluation (default is 199)
        typedQuery.maxResults = maxRes
        val items = typedQuery.resultList
        log.debug("[${entityClass.simpleName}s] $search -> ${items.size} locations (max=$maxRes)")
        return items
    }

    /**
     * Helper to enable filtering based on subclasses
     * convert from EntityType enum (which is used in [SearchRequest] to Kotlin (and later Java) class
     * @throws IllegalArgumentException if the type is not (yet) supported by LocationSearch
     */
    private fun entityTypeToClass(entityType: EntityType): KClass<out Location> {
        return when (entityType) {
            EntityType.Tour -> Tour::class
            EntityType.Video -> Video::class
            EntityType.Post -> Post::class
            EntityType.Place -> PlaceV2::class
            // More to come ... but not yet
            else -> throw IllegalArgumentException("EntityType $entityType is not yet supported for advanced search")
        }
    }

    //        val videoCount = repo.findAllByType(listOf(Video::class.java))
    //        log.info("we have $videoCount videos")

}
