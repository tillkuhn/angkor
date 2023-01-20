package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.array.ListArrayType
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType
import net.timafe.angkor.domain.enums.EntityType
import org.hibernate.annotations.Type
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*
import jakarta.persistence.*

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
    @Type(PostgreSQLEnumType::class)
    var entityType: EntityType? = null,

    @Type(ListArrayType::class)
    @Column(
        name = "keywords",
        columnDefinition = "text[]"
    )
    var tags: MutableList<String> = mutableListOf()

) {
    override fun toString() = "Tag(label=${this.label},entityType=${this.entityType})"
}

