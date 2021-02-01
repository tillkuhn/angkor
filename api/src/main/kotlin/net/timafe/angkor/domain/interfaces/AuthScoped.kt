package net.timafe.angkor.domain.interfaces

import net.timafe.angkor.domain.enums.AuthScope

/**
 * Implement this interface in authscoped entities
 */
interface AuthScoped {

    var authScope: AuthScope // abstract property

}
