package net.timafe.angkor.domain

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import net.timafe.angkor.config.annotations.EntityTypeInfo

/**
 * Entity that represents a Blog Post,
 * typically backed by an external WordPress URL
*/
@Entity
@DiscriminatorValue("Post")
@EntityTypeInfo(eventOnCreate = true, eventOnUpdate = false, eventOnDelete = true)
class Post : LocatableEntity()
