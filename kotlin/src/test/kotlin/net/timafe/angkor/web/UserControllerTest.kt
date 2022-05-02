package net.timafe.angkor.web

import net.timafe.angkor.helper.TestHelpers
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito
import java.security.Principal
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserControllerTest(private val controller: UserController) {

    fun testAuthenticated() {
        val users = controller.getUsersFromSessionRegistry()
        assertNotNull(users, "session registry should return at least empty list")


        val user = controller.getCurrentUser(TestHelpers.usernamePasswordAuthToken())
        assertNotNull(user)
        assertEquals("system", user.login)
    }

    fun `it should throw exception if getAuthentication is not called with subclass of AbstractAuth`() {
        // val token = TestHelpers.usernamePasswordAuthToken()
        val ex = Assertions.assertThrows(IllegalArgumentException::class.java) {
            controller.getAuthentication(Mockito.mock(Principal::class.java))
        }
        assertContains(ex.message!!, "AbstractAuthenticationToken expected")
    }

    fun `it should trigger request deletion mail`() {
        val res = controller.removeMe(TestHelpers.usernamePasswordAuthToken())
        assertTrue(res.result)

    }
}
