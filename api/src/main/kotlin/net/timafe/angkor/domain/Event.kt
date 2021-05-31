package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.config.Constants
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
// @EntityListeners(AuditingEntityListener::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Event(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    var entityId: UUID? = null,

    var action: String,

    var message: String? = null,

    var topic: String? = null,

    var source: String? = null,

    var partition: Int? = null,

    var recordOffset: Long? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT + "XXX")
    @CreatedDate
    var time: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var userId: UUID? = null,

//    @Enumerated(EnumType.STRING)
//    @Column(columnDefinition = "scope")
//    @Type(type = "pgsql_enum")
//    @JsonIgnore
//    override var authScope: AuthScope = AuthScope.PUBLIC
)
