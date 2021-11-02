package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Location
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.service.LocationService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


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
// How to filter a PostgreSQL array column with the JPA Criteria API?
// https://stackoverflow.com/a/24695695/4292075
// Vlad CriteriaAPITest with lots of useful code
// https://github.com/vladmihalcea/high-performance-java-persistence/blob/master/core/src/test/java/com/vladmihalcea/book/hpjp/hibernate/fetching/CriteriaAPITest.java
// JPA & Criteria API - Select only specific columns
// https://coderedirect.com/questions/99538/jpa-criteria-api-select-only-specific-columns
// https://www.baeldung.com/jpa-hibernate-projections#hibernatesinglecolumn
// How do DTO projections work with JPA and Hibernate
// https://thorben-janssen.com/dto-projections/
@RestController
@RequestMapping(Constants.API_LATEST+"/locations")
class LocationController(private val service: LocationService) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Search all items, delegates to post search with empty request
     */
    @GetMapping("search/")
    fun searchAll(): List<Location> = search(SearchRequest())

    @GetMapping("search/{query}")
    fun search(@PathVariable query: String): List<Location> = search(SearchRequest(query=query))


    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    fun search(@Valid @RequestBody search: SearchRequest): List<Location> {
        return service.search(search)
    }


}
