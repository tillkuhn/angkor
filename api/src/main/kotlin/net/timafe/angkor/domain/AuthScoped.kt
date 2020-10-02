package net.timafe.angkor.domain

import net.timafe.angkor.domain.enums.AuthScope
import java.util.*

interface AuthScoped {

    var authScope: AuthScope // abstract property
}
