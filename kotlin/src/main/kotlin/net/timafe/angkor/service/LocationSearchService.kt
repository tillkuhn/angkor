package net.timafe.angkor.service

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Selection
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.*
import net.timafe.angkor.domain.dto.LocationPOI
import net.timafe.angkor.domain.dto.LocationSummary
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.repo.LocationRepository
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
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
// How to filter a Postgres array column with the JPA Criteria API?
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
    private val locationRepo: LocationRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Returns the number of visible items of the Location Subclass
     * based on the security context's authscope
     *
     * @param entityType will be translated to the subclass to filter the query
     */
    fun visibleItemCount(entityType: EntityType): Long {
        val authScopes = SecurityUtils.allowedAuthScopes()
        val entityClass = entityTypeToClass(entityType)
        return locationRepo.itemCountByTypes(listOf(entityClass.java),authScopes)
    }

    /**
     * Returns the number of all visible Location items
     * @see visibleItemCount
     */
    fun visibleItemsWithCoordinatesCount(): Long {
        // Since this is (still) a native query, we need to pass auth scopes as string
        return locationRepo.itemsWithCoordinatesCount(SecurityUtils.allowedAuthScopesAsString())
    }
        /**
     * Search by SearchRequest,
     *
     * @return  List of LocationSummary DTOs
     * Mainly used by LocationSearchController to delegate searches triggered by the UI
     */
    fun search(search: SearchRequest): List<LocationSummary> {
        val cArgs = listOf(
            // can be directly injected to constructor
            "areaCode", "id", "imageUrl", "name", "primaryUrl", "updatedAt", "updatedBy",
            // Special params (Postgres enums, lists etc.) injected via backing helper property of type any
            "coordinates", "tags", "type", "authScope"
        )
        return search(search, LocationSummary::class, cArgs)
    }

    /**
     * Search by SearchRequest, return  location summaries
     * Mainly used by LocationSearchController to delegate searches triggered by the UI
     *
     * @return list of compact [LocationPOI] DTOs
     */
    fun searchMapLocations(search: SearchRequest): List<LocationPOI> {
        val constructorArgs = listOf("areaCode", "coordinates", "id", "imageUrl", "name", "type")
        return search(search, LocationPOI::class, constructorArgs)
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
        val entityClass = LocatableEntity::class

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
                // CAUTION: With hibernate 6 we need to explicitly set alias for every field, or we get:
                // Could not determine appropriate instantiation strategy - no matching constructor found and one or
                // more arguments did not define alias for bean-injection
                // at org.hibernate.sql.results.graph.instantiation.internal.DynamicInstantiationResultImpl
                when (coArg) {
                    // this translates into the Java Subclass (e.g. net.timafe.angkor.domain.Place)
                    "type" -> selections.add(root.type().alias(coArg))
                    // this is the default selection case
                    else -> selections.add(root.get<Any>(coArg).alias(coArg))
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
        // by using the type method of the Path Criteria API class (see tutorial from Vlad)
        if (search.entityTypes.isNotEmpty()) {
            // we need to perform some magic to get the javaClasses (required for Criteria API)
            // from the EntityType Enum List in SearchRequest
            val typeClasses = mutableListOf<Class<out LocatableEntity>>()
            for (entityType in search.entityTypes) {
                val subclass: KClass<out LocatableEntity> = entityTypeToClass(entityType)
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
        // val maxRes = Constants.JPA_DEFAULT_RESULT_LIMIT / 2 // keep it smaller for evaluation (default is 199)
        val maxRes = if (search.pageSize > Constants.JPA_MAX_RESULT_LIMIT) Constants.JPA_MAX_RESULT_LIMIT else search.pageSize
        typedQuery.maxResults = maxRes
        val items = typedQuery.resultList
        log.debug("[{}] {} -> {} locations (max={})", entityClass.simpleName, search, items.size, maxRes)
        return items
    }

    /**
     * Helper to enable filtering based on subclasses
     * convert from EntityType enum (which is used in [SearchRequest] to Kotlin (and later Java) class
     * @throws IllegalArgumentException if the type is not (yet) supported by LocationSearch
     */
    private fun entityTypeToClass(entityType: EntityType): KClass<out LocatableEntity> {
        return when (entityType) {
            EntityType.Tour -> Tour::class
            EntityType.Video -> Video::class
            EntityType.Post -> Post::class
            EntityType.Place -> Place::class
            EntityType.Photo -> Photo::class
            // More to come ... but not yet
            else -> throw IllegalArgumentException("EntityType $entityType is not yet supported for advanced search")
        }
    }


}
