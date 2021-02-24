package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.NoteStatus
import org.springframework.beans.factory.annotation.Value
import java.util.*

/**
 * Projection Interface
 * https://www.baeldung.com/spring-data-jpa-projections
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
interface NoteSummary {
    var id: UUID
    var summary: String?
    var status: NoteStatus
    var authScope: AuthScope
    var createdAt: String?
    var dueDate: String?
    var primaryUrl: String?
    var userName: String?
    var userEmail: String?
    var noteUrl: String?

    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections
    // Target is of type input: MutableMap<String, Any> (
    @Value("#{@mappingService.postgresArrayStringToList(target.tags)}")
    fun getTags(): List<String>

}
