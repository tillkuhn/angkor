package net.timafe.angkor.repo

import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.POI
import net.timafe.angkor.domain.Place
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface NoteRepository : CrudRepository<Note, UUID> {

    override fun findAll(): List<Note>
}
