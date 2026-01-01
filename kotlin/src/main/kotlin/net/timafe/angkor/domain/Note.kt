package net.timafe.angkor.domain

// import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import com.fasterxml.jackson.annotation.JsonFormat
import io.hypersistence.utils.hibernate.type.array.ListArrayType
import jakarta.persistence.*
import net.timafe.angkor.config.Constants
import net.timafe.angkor.config.annotations.EntityTypeInfo
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.NoteStatus
import net.timafe.angkor.domain.interfaces.EventSupport
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
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class, EntityEventListener::class)
@EntityTypeInfo(eventOnCreate = true, eventOnUpdate = true, eventOnDelete = true)
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
    // @Type(PostgreSQLEnumType::class)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    var status: NoteStatus = NoteStatus.OPEN,

    @Type(ListArrayType::class)
    @Column(
        name = "tags",
        columnDefinition = "text[]"
    )
    override var tags: MutableList<String> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "scope")
    // @Type(PostgreSQLEnumType::class)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    override var authScope: AuthScope = AuthScope.PUBLIC

) : Taggable, EventSupport {

    override fun description(): String {
        return this.summary
    }

    override fun toString() = "Note(id=${this.id}, name=${this.summary})"

}
