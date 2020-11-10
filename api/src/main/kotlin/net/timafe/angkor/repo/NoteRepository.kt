package net.timafe.angkor.repo

import net.timafe.angkor.domain.Note
import org.springframework.data.repository.CrudRepository
import java.util.*

interface NoteRepository : CrudRepository<Note, UUID> {

    override fun findAll(): List<Note>

}
