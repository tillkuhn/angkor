package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Note
import net.timafe.angkor.repo.NoteRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION + "/notes")
class NoteController {

    @Autowired
    private lateinit var noteRepository: NoteRepository

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


}
