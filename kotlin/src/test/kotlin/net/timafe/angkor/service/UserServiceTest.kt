package net.timafe.angkor.service

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.User
import net.timafe.angkor.repo.UserRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserServiceTest {

    private lateinit var userService: UserService
    private val user = User(id = UUID.randomUUID())

    @BeforeEach
    fun setUp() {
        user.login = this.javaClass.simpleName.lowercase()
        // https://www.baeldung.com/kotlin/mockito
        val userRepo = Mockito.mock(UserRepository::class.java)
        Mockito.`when`(userRepo.findByLoginOrEmailOrId(user.login, null, null))
            .thenReturn(listOf(user))

        userService = UserService(
            userRepository = userRepo,
            cacheService = Mockito.mock(CacheService::class.java),
        )
    }

    // https://jivimberg.io/blog/2020/07/10/effective-testing-test-structure/
    // On Given: We create the objects and set up the needed state.
    // On When: We perform the action we want to test.
    // On Then: We validate the state changed as expected.
    @Test
    fun `Given an existing service account user userService should return a token`() {

        val token = userService.getServiceAccountToken(this.javaClass)
        assertNotNull(token)
        assertEquals(user.login, token.name)
        assertEquals(user.id, token.id)
    }

    @Test
    fun `Given an none service account user userService throw an Exception`() {
        assertThrows(IllegalStateException::class.java) {
            // this must fail since there's no associated user
            userService.getServiceAccountToken(Constants.javaClass)
        }
    }

}
