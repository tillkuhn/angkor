package net.timafe.angkor.domain.interfaces

import java.util.*

/**
 * Indicates the implementing entity has EntityEvent Listener Support
 */
interface EventSupport : AuthScoped {

    var id: UUID?
    fun description(): String

}
