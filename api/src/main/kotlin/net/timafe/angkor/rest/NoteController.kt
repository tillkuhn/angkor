package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.dto.NoteSummary
import net.timafe.angkor.repo.NoteRepository
import net.timafe.angkor.service.AuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping(Constants.API_LATEST + "/notes")
class NoteController(
    private val repo: NoteRepository,
    var authService: AuthService
): ResourceController<Note, NoteSummary>  {

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
    @ResponseStatus(HttpStatus.CREATED)
    override fun createItem(item: Note): Note = repo.save(item)

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

    override fun updateItem(newItem: Note, id: UUID): ResponseEntity<Note> {
        TODO("Not yet implemented")
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

