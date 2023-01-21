package net.timafe.angkor.domain

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Event(

    @Id
    // No @GeneratedValue, since we want to re-use UUIDs from OAuth2 Provider where possible
    var id: UUID? = null,

    var action: String,

    var message: String? = null,

    var topic: String? = null,

    var entityId: UUID? = null,

    var source: String? = null,

    var partition: Int? = null,

    @Column(name = "record_offset")
    var offset: Long? = null,

    // https://stackoverflow.com/questions/41037243/how-to-make-milliseconds-optional-in-jsonformat-for-timestamp-parsing-with-jack
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    @CreatedDate
    var time: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var userId: UUID? = null,

    ) {
    companion object {
        //  Version of the event message structure which can be used in Kafka Record Message headers
        const val VERSION = "1.1"
    }
}
