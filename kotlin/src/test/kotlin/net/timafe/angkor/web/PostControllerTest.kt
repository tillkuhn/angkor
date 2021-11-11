package net.timafe.angkor.web

import net.timafe.angkor.domain.Post
import net.timafe.angkor.domain.dto.SearchRequest
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PostControllerTest(private val controller: PostController) {

    fun testCRUD() {
        val initialCount = findAll().size
        val item = Post()
        item.apply {
            name = "testVideo"
            primaryUrl = "https://some.video/1234"
            areaCode = "th"
        }
        val created = controller.create(item)
        val id = created.id
        assertEquals(initialCount+1,findAll().size)

        val lookupCreated = controller.findOne(id)
        assertEquals(HttpStatus.OK,lookupCreated.statusCode)
        assertNotNull(lookupCreated.body)
        val newName = "coolPost"
        val itemUpd = lookupCreated.body!!
        itemUpd.tags.add("asia")
        itemUpd.name = newName
        itemUpd.coordinates = arrayListOf(99.079224, 7.624368) // Krabi

        val updated = controller.save(lookupCreated.body!!,id)
        assertEquals(HttpStatus.OK,updated.statusCode)
        assertEquals(newName,updated.body!!.name)
        assertEquals(1,updated.body!!.tags.size)
        assertEquals(2,updated.body!!.coordinates.size)

        val del = controller.delete(id)
        assertEquals(HttpStatus.OK,del.statusCode)
        assertEquals(initialCount,findAll().size)
    }

    private fun findAll(): List<Post> = controller.findAll()

}
