package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.dto.NoteSummary
import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.ExternalAuthService
import net.timafe.angkor.service.NoteService
import net.timafe.angkor.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping(Constants.API_LATEST + "/notes")
class NoteController(
    private val service: NoteService,
    private val userService: UserService,
    private val externalAuthService: ExternalAuthService
) : EntityController<Note, NoteSummary, UUID>(service) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    override fun create(@RequestBody item: Note): Note {
        if (item.assignee == null) {
            val defaultAssignee = userService.getCurrentUser()?.id
            log.debug("[NOTE] Assignee not set, using current User as default: $defaultAssignee")
            item.assignee = defaultAssignee
        }
        return super.create(item)
    }

    override fun mergeUpdates(currentItem: Note, newItem: Note): Note =
        currentItem
            .copy(
                summary = newItem.summary,
                status = newItem.status,
                dueDate = newItem.dueDate,
                primaryUrl = newItem.primaryUrl,
                authScope = newItem.authScope,
                tags = newItem.tags,
                assignee = newItem.assignee
            )


    /**
     * Get notes with pending reminders
     */
    @GetMapping("reminders")
    fun reminders(@RequestHeader headers: HttpHeaders): List<NoteSummary> {
        externalAuthService.validateApiToken(headers)
        return service.noteReminders()
    }

}

