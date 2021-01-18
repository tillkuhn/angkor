package net.timafe.angkor.config

object Constants {

    const val API_ROOT = "/api"
    const val API_LATEST = "$API_ROOT/v1"

    const val API_PATH_PLACES = "places"
    const val API_PATH_FILES = "files"
    const val API_PATH_ADMIN = "admin"

    const val PROFILE_CLEAN = "clean"
    const val PROFILE_PROD = "prod"
    const val PROFILE_TEST = "test"

    const val JPA_DEFAULT_RESULT_LIMIT = 199 // Default resultlist Limit for JPA Queries,

    const val JACKSON_DATE_FORMAT = "yyyy-MM-dd"  /* should be "2019-11-08T07:08:45.134Z" */
    const val JACKSON_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"  /* should be "2019-11-08T07:08:45.134Z" */
    const val USER_ANONYMOUS = "anonymous"
    // const val LOGIN_REGEX: String = "^[_.@A-Za-z0-9-]*\$" // still needed?

    const val COGNITO_ROLE_KEY = "cognito:roles"
    const val COGNITO_USERNAME_KEY = "cognito:username"

}
