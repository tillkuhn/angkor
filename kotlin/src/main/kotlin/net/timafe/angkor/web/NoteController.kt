package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.NoteSummary
import net.timafe.angkor.domain.enums.NoteStatus
import net.timafe.angkor.service.ExternalAuthService
import net.timafe.angkor.service.NoteService
import net.timafe.angkor.service.PlaceService
import net.timafe.angkor.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(Constants.API_LATEST + "/notes")
class NoteController(
    private val service: NoteService,
    private val userService: UserService,
    private val placeService: PlaceService,
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
     * Create a new place based on details of a Note,
     * closes the Note
     */
    @PostMapping("to-place")
    @ResponseStatus(HttpStatus.CREATED) // 201
    fun createPlaceFromNote(@RequestBody note: Note): Place {
        val place = Place(
            name = "new place form note",
            summary = note.summary,
            primaryUrl = note.primaryUrl,
            authScope = note.authScope,
            createdBy = userService.getCurrentUser()?.id,
            lastVisited = null,
            notes = "copied from note ${note.id}",
            areaCode = "de",
            imageUrl = null,
            id = null,
            updatedBy = userService.getCurrentUser()?.id,

        )
        val note = service.findOne(note.id!!).get()
        note.status = NoteStatus.CLOSED
        service.save(note)
        return placeService.save(place)
    }

    /**
     * Get notes with pending reminders, mainly for external remindabot service
     */
    @GetMapping("reminders")
    fun reminders(@RequestHeader headers: HttpHeaders): List<NoteSummary> {
        externalAuthService.validateApiToken(headers)
        return service.noteReminders()
    }

}

