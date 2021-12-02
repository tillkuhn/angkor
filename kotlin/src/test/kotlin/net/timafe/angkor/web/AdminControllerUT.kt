package net.timafe.angkor.web

import net.timafe.angkor.domain.dto.BulkResult
import net.timafe.angkor.service.HongKongPhooeyService
import net.timafe.angkor.service.PhotoService
import net.timafe.angkor.service.PostService
import net.timafe.angkor.service.TourService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import kotlin.test.assertEquals

class AdminControllerUT {

    @Test
    fun `it should list all actions`() {
        val ps = mock(PhotoService::class.java)
        val ctl = AdminController(ps,mock(TourService::class.java),mock(PostService::class.java),mock(HongKongPhooeyService::class.java))
        val actions = ctl.allActions()
        assertEquals(AdminController.AdminAction.values().size,actions.size)
    }

    @Test
    fun `it should call import`() {
        val ps = mock(PhotoService::class.java)
        // Mockito’s when method does not work with void methods.
        // To create a stub that does not return anything, the doNothing method is used.
        // https://notwoods.github.io/mockk-guidebook/docs/mockito-migrate/void/
        //doNothing().`when`(ps).import() // use backticks since "when" is a reserved word in kotlin
        Mockito.`when`(ps.import()).thenReturn(BulkResult())
        val ctl = AdminController(ps,mock(TourService::class.java),mock(PostService::class.java),mock(HongKongPhooeyService::class.java))
        ctl.invokeAction(AdminController.AdminAction.IMPORT_PHOTOS)
        Mockito.verify(ps,times(1)).import()
    }

}
