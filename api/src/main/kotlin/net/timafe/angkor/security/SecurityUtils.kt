package net.timafe.angkor.security

import net.timafe.angkor.domain.AuthScoped
import net.timafe.angkor.domain.enums.AuthScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.lang.IllegalStateException

// this is the future place for static security helpers ....
/*
* Sample Auth Token
    "sub" : "3913****-****-****-****-****hase8b9c",
    "cognito:groups" : [ "eu-central-1_ILJadY8m3_Facebook", "angkor-admins" ],
    "cognito:preferred_role" : "arn:aws:iam::06********:role/angkor-cognito-role-admin",
    "cognito:roles" : [ "arn:aws:iam::06********:role/angkor-cognito-role-admin" ],
    "cognito:username" : "Facebook_16************65",
    "given_name" : "Gin",
    "name" : "Gin Tonic",
    "family_name" : "Tonic",
    "email" : "gin.tonic@gmail.com"
    "nonce" : "HIaFHPVbRPM1l3hase-****-****",
    "iss" : "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_ILJ******",
    "aud" : [ "20hase*********" ]
*/

class SecurityUtils {
    companion object {

        val log: Logger = LoggerFactory.getLogger(AuthService::class.java)

        // un getCurrentUserLogin(): Optional<String> =
        // fun getAuthorities(authentication: Authentication): List<String>? {

        /**
         * Returns true if user is not authenticated, i.e. bears the AnonymousAuthenticationToken
         * as opposed to OAuth2AuthenticationToken
         */

        fun isAnonymous(): Boolean = SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken

        /**
         * Just the opposite of isAnonyous :-)
         */
        fun isAuthenticated(): Boolean = !isAnonymous()

        fun isCurrentUserInRole(authority: String): Boolean {
            // val authentication = SecurityContextHolder.getContext().authentication
            throw IllegalStateException("Method to be implemented so $authority will be checked")
            return true
        }

        /**
         * Returns a list of AuthScopes (PUBLIC,ALL_AUTH) the user is allows to access
         */
        fun allowedAuthScopes(): List<AuthScope> =
            if (isAnonymous()) listOf(AuthScope.PUBLIC) else listOf(
                AuthScope.PUBLIC,
                AuthScope.ALL_AUTH,
                AuthScope.RESTRICTED,
                AuthScope.PRIVATE
            )

        fun allowedAuthScopesAsString(): String = authScopesAsString(allowedAuthScopes())

        /**
         * helper to convert AuthScope enum list into a String array which can be used in NativeSQL Postgres queries
         * return example: {"PUBLIC", "PRIVATE"}
         */
        fun authScopesAsString(authScopes: List<AuthScope>): String {
            return "{" + authScopes.joinToString { "\"${it.name}\"" } + "}"
        }

        /**
         * Checks if the authscope of the item argument is part of allowedAuthScopes for the current user
         */
        fun allowedToAccess(item: AuthScoped): Boolean {
            val allowed = item.authScope in allowedAuthScopes()
            if (!allowed) {
                log.warn("current user not allowed to access ${item.authScope} ${item.javaClass.simpleName} item")
            }
            return allowed
        }


    }
}
