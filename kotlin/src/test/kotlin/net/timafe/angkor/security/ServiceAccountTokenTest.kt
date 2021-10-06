package net.timafe.angkor.security

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class ServiceAccountTokenTest {
    @Test
    fun `Test serviceAccountToken with class`() {
        val id = UUID.randomUUID()
        val login = this.javaClass.simpleName.lowercase()
        val token = ServiceAccountToken(login,id)
        assertEquals(login,token.name)
        assertEquals(id,token.id)
    }
}
