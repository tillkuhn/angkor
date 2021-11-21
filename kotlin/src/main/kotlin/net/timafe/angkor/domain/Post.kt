package net.timafe.angkor.domain

import net.timafe.angkor.config.annotations.EntityTypeInfo
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.EntityListeners

/**
 * Entity that represents a Blog Post,
 * typically backed by an external WordPress URL
*/
@Entity
@DiscriminatorValue("Post")
@EntityTypeInfo(eventOnCreate = true, eventOnUpdate = false, eventOnDelete = true)
class Post : LocatableEntity()
