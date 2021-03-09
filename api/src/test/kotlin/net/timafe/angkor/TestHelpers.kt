package net.timafe.angkor

import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Note
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.enums.NoteStatus
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
            name = "hase",
            id = null,
            areaCode = "de",
            imageUrl = "http://",
            primaryUrl = "http://",
            summary = "nice place",
            notes = "come back again",
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
    }
}
