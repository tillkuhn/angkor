package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.NoteStatus
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*


@Entity
data class Note(

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: UUID?,

        var summary: String,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
        var createdAt: LocalDateTime = LocalDateTime.now(),
        var createdBy: String = Constants.USER_ANONYMOUS,

        @Enumerated(EnumType.STRING)
        @Column(columnDefinition = "status")
        @Type(type = "pgsql_enum")
        var status: NoteStatus = NoteStatus.OPEN,

        @Type(type = "list-array")
        @Column(
                name = "tags",
                columnDefinition = "text[]"
        )
        override var tags: List<String> = listOf()
) : Taggable
