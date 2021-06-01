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
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Event(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    var action: String,

    var message: String? = null,

    var topic: String? = null,

    var entityId: UUID? = null,

    var source: String? = null,

    var partition: Int? = null,

    var recordOffset: Long? = null,

    // https://stackoverflow.com/questions/41037243/how-to-make-milliseconds-optional-in-jsonformat-for-timestamp-parsing-with-jack
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    var time: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var userId: UUID? = null,

) {
    companion object {
        //  Version of the event message structure which can be used in Kafka Record Message headers
        const val VERSION = "1.0"
    }
}
