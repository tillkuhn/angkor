package net.timafe.angkor.domain.dto

import java.util.*

data class UserSummary(
    val id: UUID,
    val name: String
) {
    val shortname: String
        get() = if (name.contains(' '))
            name.split(' ')[0] + " " +  name.split(' ')[1].subSequence(0,1) + "."
        else name
}

/* Example metric repsponse
"name" : "process.uptime",
"description" : "The uptime of the Java virtual machine",
"baseUnit" : "seconds",
"measurements" : [ {
    "statistic" : "VALUE",
    "value" : 19.572
} ],
"availableTags" : [ ]

 */
