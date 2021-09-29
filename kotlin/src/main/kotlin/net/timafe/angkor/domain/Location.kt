package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.interfaces.Mappable
import net.timafe.angkor.service.EntityEventListener
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

    @Version
    open var version: Long? = null,

    open var name: String? = "dummy",

    // Audit Fields
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    open var createdAt: ZonedDateTime? = ZonedDateTime.now(),

    @CreatedBy
    open var createdBy: UUID? = UUID.fromString(Constants.USER_SYSTEM),

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @LastModifiedDate
    open var updatedAt: ZonedDateTime? = ZonedDateTime.now(),

    @LastModifiedBy
    open var updatedBy: UUID? = UUID.fromString(Constants.USER_SYSTEM),

    // coordinates for Mappable
    @Type(type = "list-array")
    @Column(
        name = "coordinates",
        columnDefinition = "double precision[]"
    )
    override var coordinates: List<Double> = listOf(), /* 0.0, 0.0 */


) : Mappable, Serializable {

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is Location -> false
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
