package net.timafe.angkor.web

import net.timafe.angkor.domain.Video
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VideoControllerTest(private val controller: VideoController) {

    fun testCRUD() {
        val initialCount = controller.findAll().size
        val video = Video()
        video.apply {
            name = "testVideo"
            primaryUrl = "https://some.video/1234"
            areaCode = "de"
        }
        val created = controller.create(video)
        val id = created.id
        assertEquals(initialCount+1,controller.findAll().size)

        val lookupCreated = controller.findOne(id)
        assertEquals(HttpStatus.OK,lookupCreated.statusCode)
        assertNotNull(lookupCreated.body)
        val newName = "coolVideo"
        val video2 = lookupCreated.body!!
        video2.tags.add("hase")
        video2.name = newName
        video2.coordinates = arrayListOf(10.0, 20.0)

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
