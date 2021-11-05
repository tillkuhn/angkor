package net.timafe.angkor.domain

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

/**
 * Entity that represents a Blog Post
 */
@Entity
@DiscriminatorValue("POST")
class Post : Location()
