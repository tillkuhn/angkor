package net.timafe.angkor.domain.interfaces

import net.timafe.angkor.domain.enums.AuthScope

/**
 * Implement this interface in auth scoped entities
 */
interface AuthScoped {

    var authScope: AuthScope // abstract property

}
