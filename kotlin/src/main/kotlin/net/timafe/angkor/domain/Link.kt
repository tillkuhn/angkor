package net.timafe.angkor.domain

// import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.array.ListArrayType
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType
import jakarta.persistence.*
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.Media_Type
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.domain.interfaces.Mappable
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.Type
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
// https://vladmihalcea.com/map-postgresql-hstore-jpa-entity-property-hibernate/
data class Link(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    var name: String?,

    var linkUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "media_type")
    // @Type(PostgreSQLEnumType::class)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    var mediaType: Media_Type = Media_Type.DEFAULT,

    var entityId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "entity_type")
    // @Type(PostgreSQLEnumType::class)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    var entityType: EntityType? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    var createdAt: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdBy: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "scope")
    // @Type(type = "pgsql_enum") ->  @Type(PostgreSQLEnumType::class)
    // @Type(PostgreSQLEnumType::class)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    override var authScope: AuthScope = AuthScope.PUBLIC,

    // @Type(type = "list-array") -> @Type(ListArrayType::class)
    @Type(ListArrayType::class)
    @Column(name = "coordinates", columnDefinition = "double precision[]")
    override var coordinates: List<Double> = listOf(),

    // @Type(type = "hstore") ->    @Type(PostgreSQLHStoreType::class)
    @Type(PostgreSQLHStoreType::class)
    @Column(columnDefinition = "hstore")
    var properties:  Map<String, String> = HashMap()

) : AuthScoped, Mappable {

    override fun toString() = "Link(id=${this.id}, mediaType=${this.mediaType}, name=${this.name})"

}

