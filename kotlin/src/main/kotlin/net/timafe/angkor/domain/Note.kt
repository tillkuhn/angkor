package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.config.Constants
import net.timafe.angkor.config.annotations.EntityTypeInfo
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.NoteStatus
import net.timafe.angkor.domain.interfaces.EventSupport
import net.timafe.angkor.domain.interfaces.Taggable
import net.timafe.angkor.service.EntityEventListener
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

@Entity
@EntityListeners(AuditingEntityListener::class, EntityEventListener::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityTypeInfo(type = EntityType.Note)
data class Note(

    @Id
    override var id: UUID = UUID.randomUUID(),

    var summary: String,
    var primaryUrl: String?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    var createdAt: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdBy: UUID?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @LastModifiedDate
    var updatedAt: ZonedDateTime? = ZonedDateTime.now(),

    @LastModifiedBy
    var updatedBy: UUID?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
    var dueDate: LocalDate?,

    var assignee: UUID?,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "status")
    @Type(type = "pgsql_enum")
    var status: NoteStatus = NoteStatus.OPEN,

    @Type(type = "list-array")
    @Column(
        name = "tags",
        columnDefinition = "text[]"
    )
    override var tags: MutableList<String> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "scope")
    @Type(type = "pgsql_enum")
    override var authScope: AuthScope = AuthScope.PUBLIC

) : Taggable, EventSupport {

    override fun description(): String {
        return this.summary
    }

    override fun toString() = "Note(id=${this.id}, name=${this.summary})"

}
