package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.vladmihalcea.hibernate.type.basic.PostgreSQLHStoreType
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.domain.interfaces.Mappable
import net.timafe.angkor.domain.interfaces.Taggable
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

/**
 * Base class for anything that qualifies as a [Location]
 *
 * Using UUID on Spring Data JPA Entities (with AbstractBaseEntity):
 *   https://jivimberg.io/blog/2018/11/05/using-uuid-on-spring-data-jpa-entities/
 *
 * Hibernate with Kotlin - powered by Spring Boot:
 *   https://kotlinexpertise.com/hibernate-with-kotlin-spring-boot/
 *
 * CAUTION: JsonIgnore works here, @JsonFormat apparently only data classes, maybe b/c of inheritance?
 *
 */
@Entity
@Table(name = "location")
@EntityListeners(AuditingEntityListener::class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    discriminatorType = DiscriminatorType.STRING,
    name = "etype"
)
@TypeDefs(
    TypeDef(
        name = "list-array",
        typeClass = com.vladmihalcea.hibernate.type.array.ListArrayType::class
    ),
    // https://vladmihalcea.com/map-postgresql-hstore-jpa-entity-property-hibernate/
    TypeDef(
        name = "hstore",
        typeClass = PostgreSQLHStoreType::class
    )
)
@JsonInclude(JsonInclude.Include.NON_NULL)
open class Location (

    open var externalId: String? = null,
    open var name: String = "",
    open var imageUrl: String? = null,
    open var primaryUrl: String? = null,

    // authscope to satisfy Interface
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "scope")
    @Type(type = "pgsql_enum")
    override var authScope: AuthScope = AuthScope.PUBLIC,

    // coordinates for Mappable
    @Type(type = "list-array")
    @Column(
        name = "coordinates",
        columnDefinition = "double precision[]"
    )
    override var coordinates: List<Double> = listOf(), /* 0.0, 0.0 */

    @Type(type = "list-array")
    @Column(
        name = "tags",
        columnDefinition = "text[]"
    )
    override var tags: MutableList<String> = mutableListOf(),

    @Type(type = "hstore")
    @Column(columnDefinition = "hstore")
    open var properties: MutableMap<String, String> = mutableMapOf(),

    // Audit Fields

    @CreatedDate
    open var createdAt: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    open var createdBy: UUID = UUID.fromString(Constants.USER_SYSTEM),

    @LastModifiedDate
    open var updatedAt: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    open var updatedBy: UUID = UUID.fromString(Constants.USER_SYSTEM),

    // Entity Version managed by persistence provider
    @Version
    @JsonIgnore
    open var version: Long = 0,

    ) : AbstractBaseEntity(),Mappable, AuthScoped, Taggable, Serializable {

    // Kotlin Dataclass Style ...
    override fun toString() =
        "${this::class.simpleName}{" +
                "id='" + id + '\'' +
                "externalId='" + externalId + '\'' +
                ", name='" + name + '\'' +
                "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}

