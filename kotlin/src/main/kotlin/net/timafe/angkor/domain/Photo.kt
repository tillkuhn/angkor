package net.timafe.angkor.domain

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import net.timafe.angkor.config.annotations.EntityTypeInfo

/**
 * Entity that represents a Photo
 * typically backed by an external 500px URL
*/
@Entity
@DiscriminatorValue("Photo")
@EntityTypeInfo(eventOnCreate = true, eventOnUpdate = false, eventOnDelete = true)
class Photo : LocatableEntity()
