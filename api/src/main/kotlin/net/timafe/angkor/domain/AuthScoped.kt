package net.timafe.angkor.domain

import net.timafe.angkor.domain.enums.AuthScope

/**
 * Implement this interface in authscoped entities
 */
interface AuthScoped {

    var authScope: AuthScope // abstract property

}
