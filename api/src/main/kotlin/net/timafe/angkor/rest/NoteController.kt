package net.timafe.angkor.rest

import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.dto.NoteSummary
import net.timafe.angkor.repo.NoteRepository
import net.timafe.angkor.service.AuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import org.springframework.web.bind.annotation.RequestHeader

@RestController
@RequestMapping(Constants.API_LATEST + "/notes")
class NoteController(
    private val repo: NoteRepository,
    private val authService: AuthService,
    private val appProperties: AppProperties
): ResourceController<Note, NoteSummary> {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

//    @GetMapping
//    @ResponseStatus(HttpStatus.OK)
//    fun allNotes(principal: Principal?): List<Note> {
//        // val dishes = if (principal != null)  placeRepository.findByOrderByName() else placeRepository.findPublicPlaces()
//        val entities = repo.findAll()
//        //  coo ${places.get(0).coordinates}"
//        log.info("allNotes() return ${entities.size} notes principal=${principal}")
//        return entities
//    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    override fun createItem(@RequestBody item: Note): Note = repo.save(item)

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun getItem(id: UUID): ResponseEntity<Note> {
        TODO("Not yet implemented")
    }

    @DeleteMapping("{id}")
    override fun deleteItem(@PathVariable(value = "id") id: UUID): ResponseEntity<Void> {
        log.debug("Deleting note item $id")
        return repo.findById(id).map { item ->
            repo.delete(item)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Updates an item, this operation needs to be adapted if we add new attributes
     */
    @PutMapping(value = ["{id}"])
    @ResponseStatus(HttpStatus.OK)
    override fun updateItem(@Valid @RequestBody newItem: Note, @PathVariable id: UUID): ResponseEntity<Note> {
        log.info("update () called for item $id")
        return repo.findById(id).map { existingItem ->
            val updatedItem: Note = existingItem
                .copy(summary = newItem.summary,
                    status  = newItem.status,
                    dueDate = newItem.dueDate,
                    primaryUrl = newItem.primaryUrl,
                    authScope = newItem.authScope,
                    tags = newItem.tags
                )
            ResponseEntity.ok().body(repo.save(updatedItem))
        }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * Get notes with pending reminders
     */
    @GetMapping("reminders")
    fun reminders(@RequestHeader headers: HttpHeaders): List<NoteSummary> {
        val authHeader = headers.get(appProperties.apiTokenHeader)?.get(0)
        if (appProperties.apiToken != authHeader) {
            val msg = "Invalid or no ${appProperties.apiTokenHeader} set, value size is ${authHeader?.length}"
            log.warn(msg)
            // todo check https://www.baeldung.com/spring-response-status-exception#1-generate-responsestatusexception
            throw ForbiddenException(msg)
        } else {
            log.trace("Authheader valid")
        }
        log.debug("Retrievieving reminders, authheader $authHeader")
        return search("")
    }


    /**
     * Search all items
     */
    @GetMapping("search/")
    fun searchAll(): List<NoteSummary> {
        return search("")
    }

    /**
     * Search by search query
     */
    @GetMapping("search/{search}")
    override fun search(@PathVariable(required = true) search: String): List<NoteSummary> {
        val authScopes = authService.allowedAuthScopesAsString()
        val items = repo.search(search, authScopes)
        log.info("allItemsSearch(${search}) return ${items.size} places authScopes=${authScopes}")
        return items
    }

}

// todo different file, different package
@ResponseStatus(HttpStatus.FORBIDDEN, reason = "Invalid or missing token")
class ForbiddenException(msg: String) : RuntimeException(msg)
