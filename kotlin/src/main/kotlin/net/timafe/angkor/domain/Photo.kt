package net.timafe.angkor.domain

import net.timafe.angkor.config.annotations.EntityTypeInfo
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

/**
 * Entity that represents a Photo
 * typically backed by an external 500px URL
*/
@Entity
@DiscriminatorValue("Photo")
@EntityTypeInfo(eventOnCreate = true, eventOnUpdate = false, eventOnDelete = true)
class Photo : LocatableEntity()