package net.timafe.angkor.domain.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserSummary(
    val id: UUID,
    @JsonIgnore // don't expose full name, see shortname below
    val name: String,
    val emoji: String
) {
    // shortname will become an exposed dynamic property with the abbreviated lastname
    val shortname: String
        get() = if (name.contains(' '))
            name.split(' ')[0] + " " + name.split(' ')[1].subSequence(0, 1) + "."
        else name
}

// Example JSON for Lady Baba ...
// {
//     "id": "987de347-d932-4065-9abc-75eca1dc334a",
//     "emoji": "👱‍",
//     "shortname": "Lady B."
// },
