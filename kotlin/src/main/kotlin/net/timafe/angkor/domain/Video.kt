package net.timafe.angkor.domain

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

/**
 * Entity that represents a Video
 * (typically backed by an external youtube url)
 */
@Entity
@DiscriminatorValue("Video")
class Video(

    @Column
    var summary: String? = null

) : LocatableEntity()
