package net.timafe.angkor.security

import net.timafe.angkor.domain.enums.AppRole
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.interfaces.AuthScoped
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.util.*
import java.util.zip.Adler32
import java.util.zip.Checksum


/*
* This is *the* place for static security helpers ....
*
* SecurityContextHolder.getContext().authentication -> returns
* Oauth2AuthenticationToken ->
* - principal {DefaultOidcUser}
*   - authorities - not the same as the ones on parent level, but SCOPE_openid and ROLE_USER
*   - nameAttributeKey = "cognito:username"
*   - attributes -> {Collections.UnmodifiableMap} key value pairs
*     - iss -> {URL@18497} "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_...."
*     - sub -> "3913..." (uuid)
*     - cognito:groups -> {JSONArray} with Cognito Group names e.g. eu-central-1blaFacebook, angkor-gurus etc.
*     - cognito:roles -> {JSONArray} similar to cognito:groups, but contains role ARNs
*     - cognito:username -> Facebook_16... (for facebook login or the login name for "direct" cognito users)
*     - given_name -> e.g. Gin
*     - family_name -> e.g. Tonic
*     - email -> e.g. gin.tonic@bla.de
* - authorities -> Collection of SimpleGrantedAuthorities with ROLE_ Prefix
* - authorizationClientRegistrationId -> cognito (could be "oidc" for other clients)
* - Details -> WebAuthenticationDetails
* - authenticated -> true
*
* Sample Auth Token:
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

        private val log: Logger = LoggerFactory.getLogger(AuthSuccessListener::class.java)

        const val COGNITO_ROLE_KEY = "cognito:roles"
        const val COGNITO_USERNAME_KEY = "cognito:username"
        const val JWT_SUBJECT_KEY = "sub"
        private const val IAM_ROLE_PATTERN = "cognito-role-"
        private val uuidRegex =
            Regex("""^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$""")

        /**
         * Returns true if user is not authenticated, i.e. bears the AnonymousAuthenticationToken
         * as opposed to OAuth2AuthenticationToken
         */
        fun isAnonymous(): Boolean =
            SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken

        /**
         * Just the opposite of isAnonymous :-)
         */
        fun isAuthenticated(): Boolean = !isAnonymous()

        /**
         * Check if current user it allowed to access the auth scoped item
         * @throws ResponseStatusException 403 exception (in case of insufficient permissions)
         */
        fun verifyAccessPermissions(item: AuthScoped) {
            val itemScope = item.authScope
            if (!allowedAuthScopes().contains(itemScope)) {
                val msg = "User's scopes ${allowedAuthScopes()} are insufficient to access ${item.authScope} items"
                log.warn(msg)
                throw ResponseStatusException(HttpStatus.FORBIDDEN, msg)
            }
        }

        /**
         * Returns a (typed) list of AuthScopes (e.g. PUBLIC,ALL_AUTH) the user is allowed to access
         */
        fun allowedAuthScopes(): List<AuthScope> {
            val scopes = mutableListOf(AuthScope.PUBLIC) // Public is always granted
            // if we have no security context, authentication is null, so we return default reduced scope
            val authentication = SecurityContextHolder.getContext().authentication ?: return scopes
            val authorities = authentication.authorities

            val isAdmin = authorities.asSequence()
                .filter { it.authority.equals(AppRole.ADMIN.withRolePrefix) }
                .any { it.authority.equals(AppRole.ADMIN.withRolePrefix) }

            val isUser = authorities.asSequence()
                .filter { it.authority.equals(AppRole.USER.withRolePrefix) }
                .any { it.authority.equals(AppRole.USER.withRolePrefix) }

            if (isAuthenticated()) scopes.add(AuthScope.ALL_AUTH)
            if (isUser) scopes.add(AuthScope.RESTRICTED)
            if (isAdmin) scopes.add((AuthScope.PRIVATE))
            return scopes
        }

        /**
         * same as allowedAuthScopes(), but returns String instead of list ...
         * Needed to support native SQL queries e.g. such as
         * ... AND auth_scope= ANY (cast(:authScopes as auth_scope[]))
         */
        fun allowedAuthScopesAsString(): String = authScopesAsString(allowedAuthScopes())

        /**
         * Convert AuthScope enum list into a String array which can be used in NativeSQL Postgres queries
         * return example: {"PUBLIC", "PRIVATE"}
         */
        fun authScopesAsString(authScopes: List<AuthScope>): String {
            return "{" + authScopes.joinToString { "\"${it.name}\"" } + "}"
        }

        /**
         * Checks if the auth scope of the item argument is part of allowedAuthScopes for the current user
         */
        fun allowedToAccess(item: AuthScoped): Boolean {
            val allowed = item.authScope in allowedAuthScopes()
            if (!allowed) {
                log.warn("current user not allowed to access ${item.authScope} ${item.javaClass.simpleName} item")
            }
            return allowed
        }

        /**
         * Given the principal's attributes, check if the expected role key (cognito:roles)
         * is present and turn the JSON Array into collection of spring compatible roles names
         * e.g. ROLE_USER, ROLE_ADMIN
         */
        fun getRolesFromAttributes(attributes: Map<String, Any>): Collection<String> {
            // Cognito groups same same but different ...
            return if (attributes.containsKey(COGNITO_ROLE_KEY /* cognito:roles */)) {
                when (val cognitoRoles = attributes[COGNITO_ROLE_KEY]) {
                    // Attention: JSONArray impl changed with Spring Boot 2.5.4
                    // we simply cast to ArrayList<Object> which both classes extend
                    is ArrayList<*> -> extractRolesFromJSONArray(cognitoRoles)
                    else -> {
                        log.warn("Unexpected $COGNITO_ROLE_KEY class: ${cognitoRoles?.javaClass} instead of JSONArray")
                        listOf() // recover with empty list
                    }
                }
            } else {
                log.warn("JWT Claim attributes do not contain key $COGNITO_ROLE_KEY")
                listOf()
            }
        }

        // roles look like arn:aws:iam::06*******:role/angkor-cognito-role-guest
        // therefore we just take the last part after cognito-role- and uppercase it, e.g.
        // example element arn:aws:iam::01234566:role/angkor-cognito-role-user
        private fun extractRolesFromJSONArray(jsonArray: ArrayList<*>): List<String> {
            return jsonArray
                .filter { it.toString().contains(IAM_ROLE_PATTERN) }
                .map {
                    "ROLE_" + it.toString().substring(
                        it.toString().indexOf(IAM_ROLE_PATTERN) + IAM_ROLE_PATTERN.length
                    )
                        .uppercase()
                }
        }

        /**
         * Returns the current user login based on the Principal's username or attributes
         */
        fun getCurrentUserLogin(): Optional<String> =
            Optional.ofNullable(extractPrincipal(SecurityContextHolder.getContext().authentication))

        /**
         * Extract the principal from the Authentication's Principal member,
         * which may be either of type UserDetails, DefaultOidcUser, or simply a String
         */
        private fun extractPrincipal(authentication: Authentication?): String? {
            if (authentication == null) {
                return null
            }
            return when (val principal = authentication.principal) {
                is UserDetails -> principal.username
                // is JwtAuthenticationToken -> (authentication as JwtAuthenticationToken).token.claims as String
                is DefaultOidcUser -> {
                    if (principal.attributes.containsKey(COGNITO_USERNAME_KEY)) {
                        principal.attributes[COGNITO_USERNAME_KEY].toString()
                    } else {
                        null
                    }
                }
                is String -> principal
                else -> null
            }
        }

        /**
         * if the input string matches pattern for uuid,
         * return the typed UUID object,
         * example: 24F25637-F2F6-4C31-B4DE-A2C8BEF1BAA4
         */
        fun safeConvertToUUID(subject: String?): UUID? {
            return if (subject != null && uuidRegex.matches(subject)) UUID.fromString(subject) else null
        }

        /**
         * Returns the MD5 Digest for the String (Hex encoded)
         */
        fun getMD5Digest(message: String): String {
            val messageDigest: MessageDigest = MessageDigest.getInstance("MD5") // "SHA-256"
            val digest = messageDigest.digest(message.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }

        /**
         * Returns Adler32 Checksum for the String
         *
         * https://www.java-examples.com/generate-adler32-checksum-byte-array-example
         */
        fun getAdler32Checksum(message: String): Long {
            val checksum: Checksum = Adler32()
            val bytes = message.toByteArray()
            checksum.update(bytes,0,bytes.size)
            return checksum.value
        }

    }
}
