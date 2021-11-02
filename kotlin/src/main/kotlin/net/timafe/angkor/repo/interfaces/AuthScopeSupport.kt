package net.timafe.angkor.repo.interfaces

import net.timafe.angkor.domain.enums.AuthScope

interface AuthScopeSupport<ET> {
    fun findAll(authScopes: List<AuthScope>): List<ET>
}
