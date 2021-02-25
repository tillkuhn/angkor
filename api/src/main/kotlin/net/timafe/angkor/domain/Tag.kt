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
data class Tag(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    var label: String,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "entity_type")
    @Type(type = "pgsql_enum")
    var entityType: EntityType? = null,

    @Type(type = "list-array")
    @Column(
        name = "keywords",
        columnDefinition = "text[]"
    )
     var tags: MutableList<String> = mutableListOf<String>()

) {
    override fun toString() = "Tag(label=${this.label},entityType=${this.entityType})"
}

