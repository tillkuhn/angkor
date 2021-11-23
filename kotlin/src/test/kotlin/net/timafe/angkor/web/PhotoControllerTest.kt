package net.timafe.angkor.web

import net.timafe.angkor.domain.Photo
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PhotoControllerTest(private val controller: PhotoController) {

    fun testCRUD() {
        val initialCount = controller.findAll().size
        val photo = Photo()
        photo.apply {
            name = "testPhoto"
            primaryUrl = "https://some.Photo/1234"
            areaCode = "de"
        }
        val created = controller.create(photo)
        val id = created.id
        assertEquals(initialCount+1,controller.findAll().size)

        val lookupCreated = controller.findOne(id)
        assertEquals(HttpStatus.OK,lookupCreated.statusCode)
        assertNotNull(lookupCreated.body)
        val newName = "coolPhoto"
        val photo2 = lookupCreated.body!!
        photo2.tags.add("hase")
        photo2.name = newName
        photo2.coordinates = arrayListOf(10.0, 20.0)

        val updated = controller.save(lookupCreated.body!!,id)
        assertEquals(HttpStatus.OK,updated.statusCode)
        assertEquals(newName,updated.body!!.name)
        assertEquals(1,updated.body!!.tags.size)
        assertEquals(2,updated.body!!.coordinates.size)

        val del = controller.delete(id)
        assertEquals(HttpStatus.OK,del.statusCode)
        assertEquals(initialCount,controller.findAll().size)
    }

}
