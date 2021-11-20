package net.timafe.angkor.helper

import net.minidev.json.JSONArray
import net.timafe.angkor.domain.*
import net.timafe.angkor.domain.enums.AppRole
import net.timafe.angkor.domain.enums.NoteStatus
import net.timafe.angkor.security.SecurityUtils
import org.mockito.Mockito
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.net.ServerSocket
import java.util.*

class TestHelpers {
    companion object {

        const val MOCK_USER = "hase"
        private val someUser: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")

        fun somePlace(): Place {
            val somePlace = Place(notes = "come back again")
            somePlace.apply {
                name = "hase"
                id = UUID.randomUUID()
                areaCode = "de"
                beenThere = null
                imageUrl = "https://hase.de"
                primaryUrl = "https://hase.de"
                summary = "nice place"
                createdBy = someUser
                updatedBy = someUser
            }
            return somePlace
        }

        fun someDish(): Dish =  Dish(
            name = "some food",
            id = UUID.randomUUID(),
            areaCode = "de",
            imageUrl = "https://some.image.io",
            primaryUrl = "https://some.url.io",
            summary = "nice cooking dish",
            notes = "use salt",
            createdBy = someUser,
            updatedBy = someUser,
            timesServed = 3
        )

        fun someNote(): Note =  Note(
            id = UUID.randomUUID(),
            primaryUrl = "http://",
            summary = "nice place",
            createdBy = someUser,
            updatedBy = someUser,
            status = NoteStatus.OPEN,
            dueDate = null,
            assignee = someUser
        )


        fun someLink(): Link =  Link(
            linkUrl = "http://some.test.lik",
            name = "testlink",
            coordinates = arrayListOf(1.0,2.0)
        )

        fun someEvent(): Event =  Event(
            id = UUID.randomUUID(),
            entityId = UUID.randomUUID(),
            userId = someUser,
            action = "test:event",
            message = "This is just a test event",
        )

        fun somePrincipalAttributes(): Map<String,Any> {
            val email = "gin.tonic@monkey.com"
            val firstname = "gin"
            val lastname = "tonic"
            val uuid = "16D2D553-5842-4392-993B-4EA0E7E7C452"
            val roles = JSONArray()
            roles.add("arn:aws:iam::012345678:role/schnickschnack-cognito-role-user")
            roles.add("arn:aws:iam::012345678:role/schnickschnack-cognito-role-admin")
            return mapOf(
                "groups" to AppRole.USER.withRolePrefix,
                "sub" to uuid,
                SecurityUtils.COGNITO_USERNAME_KEY to "Facebook_ginton123",
                SecurityUtils.COGNITO_ROLE_KEY to roles,
                "given_name" to firstname,
                "family_name" to lastname,
                "email" to email,
            )
        }

        fun usernamePasswordAuthToken(): UsernamePasswordAuthenticationToken {
            // val attributes = TestHelpers.somePrincipalAttributes()
            // val idToken = OidcIdToken(OidcParameterNames.ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), attributes)
            // val token = SecurityContextHolder.getContext().authentication
            // getAttributesForUsernamePasswordAuth
            val authorities = SecurityUtils.getRolesFromAttributes(somePrincipalAttributes())
                .map { SimpleGrantedAuthority(it) }
            val prince =
                org.springframework.security.core.userdetails.User("system", "malacca", true,
                    true, true, true, authorities)
            return UsernamePasswordAuthenticationToken(prince, "malacca")
        }
        /**
         * WireMock is a popular library for stubbing web services. It runs an HTTP server that acts as
         * an actual web service. We just set up expectations and run the server.
         * https://dzone.com/articles/kotlin-wiremock
         */
        fun findRandomWiremockPort(): Int {
            ServerSocket(0).use { socket -> return socket.localPort }
        }

        // https://stackoverflow.com/questions/30305217/is-it-possible-to-use-mockito-in-kotlin
        fun <T> mockitoAny(): T {
            return Mockito.any()
        }

    }
}
