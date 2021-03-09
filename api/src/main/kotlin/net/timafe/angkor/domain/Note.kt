package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.config.Constants
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
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@EntityListeners(AuditingEntityListener::class, EntityEventListener::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Note(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    override var id: UUID?,

    var summary: String,
    var primaryUrl: String?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @CreatedBy
    var createdBy: UUID?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @LastModifiedDate
    var updatedAt: LocalDateTime? = LocalDateTime.now(),

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
    override var tags: MutableList<String> = mutableListOf<String>(),

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "scope")
    @Type(type = "pgsql_enum")
    override var authScope: AuthScope = AuthScope.PUBLIC

) : Taggable, EventSupport {
    override fun entitySummary(): String {
        return "${this.summary}"
    }

    override fun entityType(): EntityType {
        return EntityType.DISH
    }

    override fun toString() = "Note(id=${this.id}, name=${this.summary})"

}
