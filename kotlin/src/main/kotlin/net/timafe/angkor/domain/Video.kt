package net.timafe.angkor.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import net.timafe.angkor.config.annotations.EntityTypeInfo

/**
 * Entity that represents a Video
 * (typically backed by an external youtube url)
 */
@Entity
@DiscriminatorValue("Video")
@EntityTypeInfo(eventOnCreate = true, eventOnUpdate = true, eventOnDelete = true)
class Video(

    @Column
    var summary: String? = null

) : LocatableEntity()
