package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Note
import net.timafe.angkor.repo.NoteRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*


@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION + "/notes")
class NoteController(
        private val noteRepository: NoteRepository
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun allNotes(principal: Principal?): List<Note> {
        // val dishes = if (principal != null)  placeRepository.findByOrderByName() else placeRepository.findPublicPlaces()
        val entities = noteRepository.findAll()
        //  coo ${places.get(0).coordinates}"
        log.info("allNotes() return ${entities.size} notes principal=${principal}")
        return entities
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createNewNote(@RequestBody note: Note): Note = noteRepository.save(note)

    @DeleteMapping("{id}")
    fun deleteNote(@PathVariable(value = "id") id: UUID): ResponseEntity<Void> {
        log.debug("Deleting note item $id")
        return noteRepository.findById(id).map { item ->
            noteRepository.delete(item)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }
}

