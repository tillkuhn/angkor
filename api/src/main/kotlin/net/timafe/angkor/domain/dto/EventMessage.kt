package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.config.Constants
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventMessage(
    val action: String,
    val message: String?,
    var source: String? = null,  // will be set to appName by EventService if null
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_TIME_FORMAT)
    val time: LocalDateTime = LocalDateTime.now(),
    var entityId: String? = null,
)
