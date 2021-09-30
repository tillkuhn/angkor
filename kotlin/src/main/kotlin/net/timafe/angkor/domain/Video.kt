package net.timafe.angkor.domain

import org.springframework.data.repository.CrudRepository
import java.util.*
import javax.persistence.Column

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity


@Entity
@DiscriminatorValue("VIDEO")
class Video(
    id: UUID = UUID.randomUUID(),

    @Column
    var summary: String? = null
) : Location(id)

interface VideoRepository : CrudRepository<Video, UUID>
