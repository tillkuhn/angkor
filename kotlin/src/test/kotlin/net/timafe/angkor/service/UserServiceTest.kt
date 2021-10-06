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

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `Test serviceAccountToken from UserRepo User`() {
        val user = User(id = UUID.randomUUID())
        user.login = this.javaClass.simpleName.lowercase()
        // https://www.baeldung.com/kotlin/mockito
        val userRepo = Mockito.mock(UserRepository::class.java)
        Mockito.`when`(userRepo.findByLoginOrEmailOrId(user.login,null,null))
            .thenReturn(listOf(user))

        userService = UserService(
            userRepository = userRepo,
            cacheService = Mockito.mock(CacheService::class.java),
        )

        val token = userService.getServiceAccountToken(this.javaClass)
        assertNotNull(token)
        assertEquals(user.login,token.name)
        assertEquals(user.id,token.id)
        assertThrows(IllegalStateException::class.java) {
            // this must fail since there's no associated user
            val token = userService.getServiceAccountToken(Constants.javaClass)
        }
    }
}
