package net.timafe.angkor.domain

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.EntityListeners

/**
 * Entity that represents a Blog Post,
 * typically backed by an external WordPress URL
*/
@Entity
@EntityListeners(AuditingEntityListener::class)
@DiscriminatorValue("Post")
class Post : LocatableEntity()
