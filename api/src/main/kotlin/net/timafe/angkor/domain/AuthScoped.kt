package net.timafe.angkor.domain

import net.timafe.angkor.domain.enums.AuthScope

interface AuthScoped {

    var authScope: AuthScope // abstract property

}
