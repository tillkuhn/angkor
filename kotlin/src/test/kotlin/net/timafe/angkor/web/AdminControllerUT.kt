package net.timafe.angkor.web

import net.timafe.angkor.service.PhotoService
import net.timafe.angkor.service.TourService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import kotlin.test.assertEquals

class AdminControllerUT {

    @Test
    fun `it should list all actions`() {
        val ps = Mockito.mock(PhotoService::class.java)
        val ctl = AdminController(ps,Mockito.mock(TourService::class.java))
        val actions = ctl.allActions()
        assertEquals(AdminController.AdminAction.values().size,actions.size)
    }

    @Test
    fun `it should call import`() {
        val ps = Mockito.mock(PhotoService::class.java)
        // Mockito’s when method doesn’t work with void methods.
        // To create a stub that doesn’t return anything, the doNothing method is used.
        // https://notwoods.github.io/mockk-guidebook/docs/mockito-migrate/void/
        doNothing().`when`(ps).import() // use backticks since "when" is a reserved word in koltin
        val ctl = AdminController(ps,Mockito.mock(TourService::class.java))
        ctl.invokeAction(AdminController.AdminAction.IMPORT_PHOTOS)
        Mockito.verify(ps,times(1)).import()
    }

}
