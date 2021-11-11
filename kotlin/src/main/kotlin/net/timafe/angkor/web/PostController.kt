package net.timafe.angkor.web

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Post
import net.timafe.angkor.service.PostService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * REST controller for managing [Post]s.
 */
@RestController
@RequestMapping(Constants.API_LATEST + "/posts")
class PostController(
    private val service: PostService,
) : AbstractEntityController<Post, Post, UUID>(service) { // no Summary yet!

    // Todo should be supported by superclass
    @GetMapping
    fun findAll(): List<Post> {
        return service.findAll()
    }

    // Todo duplicate code
    override fun mergeUpdates(currentItem: Post, newItem: Post): Post =
        currentItem.apply {
            name = newItem.name
            primaryUrl = newItem.primaryUrl
            imageUrl = newItem.imageUrl
            authScope = newItem.authScope
            coordinates = newItem.coordinates
            tags = newItem.tags
        }

}
