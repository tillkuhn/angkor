package net.timafe.angkor.domain.dto


import java.util.*

data class LocationSummary(
    val id: UUID,
    val name: String,
) {
    var entityType: String =""

    constructor(id: UUID, name: String, type: Class<Any>) : this(id, name) {
        this.entityType = type.simpleName.uppercase()
    }
}

