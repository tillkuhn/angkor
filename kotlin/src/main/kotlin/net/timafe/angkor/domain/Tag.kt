package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.domain.enums.EntityType
import org.hibernate.annotations.Type
import org.springframework.data.jpa.domain.support.AuditingEntityListener
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
    var tags: MutableList<String> = mutableListOf()

) {
    override fun toString() = "Tag(label=${this.label},entityType=${this.entityType})"
}

