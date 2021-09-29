package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.domain.interfaces.Mappable
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.CrudRepository
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

/**
 * https://jivimberg.io/blog/2018/11/05/using-uuid-on-spring-data-jpa-entities/
 * https://kotlinexpertise.com/hibernate-with-kotlin-spring-boot/
 * https://vladmihalcea.com/the-best-way-to-map-the-discriminatorcolumn-with-jpa-and-hibernate/

 */
@Entity
@Table(name = "location")
@EntityListeners(AuditingEntityListener::class)
@TypeDef(
    name = "list-array",
    typeClass = com.vladmihalcea.hibernate.type.array.ListArrayType::class
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    discriminatorType = DiscriminatorType.STRING,
    name = "ltype"
)
open class Location(
    @Id
    open var id: UUID = UUID.randomUUID(),

    open var name: String = "",
    open var imageUrl: String? = null,
    open var primaryUrl: String? = null,

    // authscope for Authscoped Interface
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

    // Audit Fields

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    open var createdAt: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    open var createdBy: UUID = UUID.fromString(Constants.USER_SYSTEM),

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @LastModifiedDate
    open var updatedAt: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    open var updatedBy: UUID = UUID.fromString(Constants.USER_SYSTEM),

    // Entity Version managed by persitence provider
    @Version
    open var version: Long = 0,

    ) : Mappable, AuthScoped, Serializable {

    // this is safe since we control the UUID, i.e. set it
    // even when the entity is not yet persisted
    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is Location -> false
            // see comment on hashCode
            else -> id == other.id
        }
    }

    override fun toString() =
        "${this::class.simpleName}{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}

interface LocationRepository : CrudRepository<Location, UUID>
