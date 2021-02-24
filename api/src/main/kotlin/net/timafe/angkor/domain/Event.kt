package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.EventType
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
data class Event(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    var entityId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "entity_type")
    @Type(type = "pgsql_enum")
    var entityType: EntityType? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "event_type")
    @Type(type = "pgsql_enum")
    var eventType: EventType? = null,

    var summary: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @CreatedBy
    var createdBy: UUID? = null
)
