package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


@Entity
data class Note(

        // https://vladmihalcea.com/uuid-identifier-jpa-hibernate/
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: UUID?,

        var notes: String,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
        var createdAt: LocalDateTime? = LocalDateTime.now()
)
