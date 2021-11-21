package net.timafe.angkor.domain

import net.timafe.angkor.config.annotations.EntityTypeInfo
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

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
