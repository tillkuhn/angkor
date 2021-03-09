package net.timafe.angkor.domain.interfaces

import net.timafe.angkor.domain.enums.EntityType
import java.util.*

interface EventSupport : AuthScoped {

    var id: UUID?
    fun entitySummary(): String
    fun entityType(): EntityType

}
