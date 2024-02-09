package net.timafe.angkor.config

import net.timafe.angkor.domain.enums.EntityType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class SecurityConfigUT {

    private val bap = BasicAuthenticationProvider("promi-dinner")

    @Test
    fun `it should create entity patterns for each entity type`() {
        val sc = SecurityConfig(bap)
        val patterns = sc.getEntityPatterns("/hase")
        Assertions.assertThat(patterns.size).isEqualTo(EntityType.entries.size)
    }

    @Test
    fun `it should validate metrics test user`() {
        val auth = UsernamePasswordAuthenticationToken("prometheus","promi-dinner")
        Assertions.assertThat(bap.authenticate(auth).isAuthenticated).isTrue()
        val authWrong = UsernamePasswordAuthenticationToken("prometheus","amy-wong")
        org.junit.jupiter.api.Assertions.assertThrows(BadCredentialsException::class.java) {
            bap.authenticate(authWrong)
        }
    }

}
