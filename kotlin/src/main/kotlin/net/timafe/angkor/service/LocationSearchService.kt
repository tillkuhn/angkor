package net.timafe.angkor.service

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Location
import net.timafe.angkor.domain.Post
import net.timafe.angkor.domain.Tour
import net.timafe.angkor.domain.Video
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.persistence.EntityManager
import javax.persistence.criteria.Order
import javax.persistence.criteria.Predicate
import javax.validation.Valid
import kotlin.reflect.KClass

/**
 * LocationService, mainly responsible for dealing
 * with Polymorphic location queries
 */
// Vlad: How to query by entity type using JPA Criteria API
// https://vladmihalcea.com/query-entity-type-jpa-criteria-api/
// Baeldung: https://www.baeldung.com/hibernate-criteria-queries
// "All JPQL queries are polymorphic." (so we also get subclass fields)
// https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/jpql-polymorphic-queries.html
// Dynamic, typesafe queries in JPA 2.0
// How the Criteria API builds dynamic queries and reduces run-time failures
// https://developer.ibm.com/articles/j-typesafejpa/
// How to filter a PostgreSQL array column with the JPA Criteria API?
// https://stackoverflow.com/a/24695695/4292075
// Vlad CriteriaAPITest with lots of useful code
// https://github.com/vladmihalcea/high-performance-java-persistence/blob/master/core/src/test/java/com/vladmihalcea/book/hpjp/hibernate/fetching/CriteriaAPITest.java
// JPA & Criteria API - Select only specific columns
// https://coderedirect.com/questions/99538/jpa-criteria-api-select-only-specific-columns
// https://www.baeldung.com/jpa-hibernate-projections#hibernatesinglecolumn
// How do DTO projections work with JPA and Hibernate
// https://thorben-janssen.com/dto-projections/
//
// Making JPA Criteria API less awkward with Kotlin
// http://lifeinide.com/post/2021-04-29-making-jpa-criteria-api-less-awkward-with-kotlin/
@Service
class LocationSearchService(
    private val entityManager: EntityManager,
   // private val repo: LocationRepository,
    ) {
    private val log = LoggerFactory.getLogger(javaClass)

//    @PostConstruct
//    fun init() {
//        val videoCount = repo.findAllByType(listOf(Video::class.java))
//        log.info("we have $videoCount videos")
//    }

    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    fun search(@Valid @RequestBody search: SearchRequest): List<Location> {

        val resultClass = Location::class
        // val resultClass = Location::class
        val entityClass = Location::class

        // // Create query
        val cBuilder = entityManager.criteriaBuilder
        val cQuery= cBuilder.createQuery(resultClass.java) // : CriteriaQuery<ResultClass>

        // // Define FROM clause
        val root = cQuery.from(entityClass.java)

        // Define DTO projection if result class is different (experimental, see links on class level)
        if (resultClass != entityClass) {
            cQuery.select(
                cBuilder.construct(
                    resultClass.java,
                    root.get<Any>("id"),
                    root.get<String>("name"),
                    root.type() // this translates into the Java Subclass (e.g. Place)
                    // you can also add functions like lower, concat etc.
                    // cBuilder.concat(author.get(Author_.firstName), ' ', author.get(Author_.lastName))
                )
            )
        }

        // the main case-insensitive "like" query against name and potentially other freetext fields
        val andPredicates = mutableListOf<Predicate>()
        if (search.query.isNotEmpty()) {
            // use lower: https://stackoverflow.com/q/43089561/4292075
            val queryPredicates = mutableListOf<Predicate>()
            queryPredicates.add(cBuilder.like(cBuilder.lower(root.get("name")), "%${search.query.lowercase()}%"))
            val tagArray = cBuilder.function("text_array",String::class.java, root.get<Any>("tags"))
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

        // Construct where clause (if there's at least one predicate)
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
        val maxRes = Constants.JPA_DEFAULT_RESULT_LIMIT / 2 // keep it small for evaluation (default is 199)
        typedQuery.maxResults = maxRes
        val items = typedQuery.resultList
        log.debug("[${entityClass.simpleName}s] Search '$search': ${items.size} results (limit $maxRes")
        return items
    }

    // Helper to convert from EntityType enum to Kotlin (and later Java) class
    private fun entityTypeToClass(entityType: EntityType): KClass<out Location> {
        return when (entityType) {
            EntityType.TOUR -> Tour::class
            EntityType.VIDEO -> Video::class
            EntityType.POST -> Post::class
            // More to come
            else -> throw IllegalArgumentException("EntityType $entityType not yet supported")
        }
    }

}
