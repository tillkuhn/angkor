package net.timafe.angkor.web

import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.security.SecurityUtils
import org.mockito.Mockito
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.security.Principal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class UserControllerTest(private val controller: UserController) {

    fun testAuthenticated() {
        val users = controller.getUsersFromSessionRegistry()
        assertNotNull(users, "session registry should return at least empty list")

        // val attributes = TestHelpers.somePrincipalAttributes()
        // val idToken = OidcIdToken(OidcParameterNames.ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), attributes)
        // val token = SecurityContextHolder.getContext().authentication
        // getAttributesForUsernamePasswordAuth
        val authorities = SecurityUtils.getRolesFromAttributes(TestHelpers.somePrincipalAttributes()).map { SimpleGrantedAuthority(it) }
        val prince =  org.springframework.security.core.userdetails.User("system","malacca",true,true,true,true,authorities)
        val token = UsernamePasswordAuthenticationToken(prince,"malacca")
        val user = controller.getCurrentUser(token)
        assertNotNull(user)
        assertEquals("system",user.login)
    }

}
