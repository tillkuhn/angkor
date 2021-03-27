package net.timafe.angkor.helper

import net.minidev.json.JSONArray
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Link
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.enums.AppRole
import net.timafe.angkor.domain.enums.NoteStatus
import net.timafe.angkor.security.SecurityUtils
import java.util.*

class TestHelpers {
    companion object {
        val someUser: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")

        fun somePlace(): Place =  Place(
            name = "hase",
            id = null,
            areaCode = "de",
            lastVisited = null,
            imageUrl = "http://",
            primaryUrl = "http://",
            summary = "nice place",
            notes = "come back again",
            createdBy = someUser,
            updatedBy = someUser
        )

        fun someDish(): Dish =  Dish(
            name = "some food",
            id = null,
            areaCode = "de",
            imageUrl = "http://",
            primaryUrl = "http://",
            summary = "nice cooking dish",
            notes = "use salt",
            createdBy = someUser,
            updatedBy = someUser,
            timesServed = 3
        )

        fun someNote(): Note =  Note(
            id = null,
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


    }
}
