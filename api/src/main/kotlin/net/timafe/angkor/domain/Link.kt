package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.LinkMediaType
import net.timafe.angkor.domain.interfaces.AuthScoped
import net.timafe.angkor.domain.interfaces.Mappable
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Link(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    var name: String?,

    var linkUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "media_type")
    @Type(type = "pgsql_enum")
    var mediaType: LinkMediaType = LinkMediaType.DEFAULT,

    var entityId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "entity_type")
    @Type(type = "pgsql_enum")
    var entityType: EntityType? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @CreatedBy
    var createdBy: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "scope")
    @Type(type = "pgsql_enum")
    override var authScope: AuthScope = AuthScope.PUBLIC,

    @Type(type = "list-array")
    @Column(name = "coordinates", columnDefinition = "double precision[]")
    override var coordinates: List<Double> = listOf()

) : AuthScoped, Mappable {

    override fun toString() = "Link(id=${this.id}, mediaType=${this.mediaType}, name=${this.name})"

}

