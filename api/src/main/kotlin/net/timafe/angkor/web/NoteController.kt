package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
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
) : ResourceController<Note, NoteSummary> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    override fun create(@RequestBody item: Note): Note {
        if (item.assignee == null) {
            val defaultAssignee = userService.getCurrentUser()?.id
            log.debug("Assignee not set, using current User as default: $defaultAssignee")
            item.assignee = defaultAssignee
        }
        return service.save(item)
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun findOne(id: UUID): ResponseEntity<Note> {
        return service.findOne(id).map { item ->
            if (SecurityUtils.allowedToAccess(item)) ResponseEntity.ok(item) else ResponseEntity.status(HttpStatus.FORBIDDEN)
                .build()
        }.orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("{id}")
    override fun delete(@PathVariable(value = "id") id: UUID): ResponseEntity<Void> {
        log.debug("Deleting note item $id")
        return service.findOne(id).map {
            service.delete(id)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Updates an item, this operation needs to be adapted if we add new attributes
     */
    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    override fun save(@Valid @RequestBody newItem: Note, @PathVariable id: UUID): ResponseEntity<Note> {
        log.info("update () called for item $id")
        return service.findOne(id).map { existingItem ->
            val updatedItem: Note = existingItem
                .copy(
                    summary = newItem.summary,
                    status = newItem.status,
                    dueDate = newItem.dueDate,
                    primaryUrl = newItem.primaryUrl,
                    authScope = newItem.authScope,
                    tags = newItem.tags,
                    assignee = newItem.assignee
                )
            ResponseEntity.ok().body(service.save(updatedItem))
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Get notes with pending reminders
     */
    @GetMapping("reminders")
    fun reminders(@RequestHeader headers: HttpHeaders): List<NoteSummary> {
        externalAuthService.validateApiToken(headers)
        return service.noteReminders()
    }

    /**
     * Search all items
     */
    @GetMapping("search/")
    fun searchAll(): List<NoteSummary> {
        return search(SearchRequest()) // Search with default request (empty string)
    }

    /**
     * Search by flexible POST SearchRequest query
     */
    @PostMapping("search")
    override fun search(@Valid @RequestBody search: SearchRequest): List<NoteSummary> = service.search(search)

}

