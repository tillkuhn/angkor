package net.timafe.angkor.domain

// import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
// import org.hibernate.annotations.TypeDef
// import org.hibernate.annotations.TypeDefs
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.hypersistence.utils.hibernate.type.array.ListArrayType
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType
import jakarta.persistence.*
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.domain.interfaces.EventSupport
import net.timafe.angkor.domain.interfaces.Mappable
import net.timafe.angkor.domain.interfaces.Taggable
import net.timafe.angkor.service.EntityEventListener
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.Type
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*

/**
 * Base class for anything that qualifies as a [LocatableEntity]
 *
 * Using UUID on Spring Data JPA Entities (with AbstractBaseEntity):
 *   https://jivimberg.io/blog/2018/11/05/using-uuid-on-spring-data-jpa-entities/
 *
 * Hibernate with Kotlin - powered by Spring Boot:
 *   https://kotlinexpertise.com/hibernate-with-kotlin-spring-boot/
 *
 * CAUTION: JsonIgnore works here, @JsonFormat apparently only on data classes (maybe b/c of inheritance?)
 *
 */
@Entity
@Table(name = "location")
@EntityListeners(AuditingEntityListener::class, EntityEventListener::class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    discriminatorType = DiscriminatorType.STRING,
    name = "etype"
)
// TypeDefs and TypeDef has been removed with Hibernate 6
// https://github.com/vladmihalcea/hypersistence-utils/issues/514
// https://stackoverflow.com/questions/70036475/what-should-i-replace-the-hibernate-deprecated-typedef-and-type-annotations-by
// https://github.com/vladmihalcea/hypersistence-utils
// https://vladmihalcea.com/postgresql-array-java-list/
/*
@TypeDefs(
    TypeDef(
        name = "list-array",
        typeClass = ListArrayType::class
    ),
    // How to map a PostgreSQL HStore entity property with JPA and Hibernate
    // https://vladmihalcea.com/map-postgresql-hstore-jpa-entity-property-hibernate/
    TypeDef(
        name = "hstore",
        typeClass = PostgreSQLHStoreType::class
    )
)

*/
@JsonInclude(JsonInclude.Include.NON_NULL)
open class LocatableEntity(

    open var externalId: String? = null,
    open var name: String = "",
    open var imageUrl: String? = null,
    open var primaryUrl: String? = null,
    open var areaCode: String? = null,
    open var geoAddress: String? = null,

    // authscope to satisfy Interface
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "scope")
    // https://vladmihalcea.com/the-best-way-to-map-an-enum-type-with-jpa-and-hibernate/
    // @Type(PostgreSQLEnumType::class)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    override var authScope: AuthScope = AuthScope.PUBLIC,

    /**
     * Coordinates for Mappable Interface
     * order is similar to GeoJSON position coordinates (Lon,Lat)
     *
     * See also [net.timafe.angkor.domain.dto.Coordinates] Wrapper Class
     */
    @Type(ListArrayType::class)
    @Column(
        name = "coordinates",
        columnDefinition = "double precision[]"
    )
    override var coordinates: List<Double> = listOf(),

    @Type(ListArrayType::class)
    @Column(
        name = "tags",
        columnDefinition = "text[]"
    )
    override var tags: MutableList<String> = mutableListOf(),

    // https://vladmihalcea.com/map-postgresql-hstore-jpa-entity-property-hibernate/
    @Type(PostgreSQLHStoreType::class)
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

    ) : AbstractBaseEntity(), Mappable, AuthScoped, Taggable, EventSupport, Serializable {

    // expose the Concrete class (useful for UI)
    @JsonProperty
    fun entityType() = EntityType.fromEntityClass(this.javaClass)

    fun hasCoordinates(): Boolean {
        return this.coordinates.size > 1
    }

    // human friendly description, mainly for Event Support
    override fun description(): String {
        return "${this.name} (${this.areaCode})"
    }

    // Kotlin Dataclass Style toString ...
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

