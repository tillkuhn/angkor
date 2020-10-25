package net.timafe.angkor.config

object Constants {
    const val API_ROOT = "/api"
    const val API_DEFAULT_VERSION = API_ROOT + "/v1"
    const val API_PATH_PLACES = "places"

    const val PROFILE_CLEAN = "clean"
    const val PROFILE_PROD = "prod"
    const val PROFILE_TEST = "test"
    const val PROFILE_OFFLINE = "offline"
    const val JACKSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"  /* should be "2019-11-08T07:08:45.134Z" */
    const val USER_ANONYMOUS = "anonymous"
    const val LOGIN_REGEX: String = "^[_.@A-Za-z0-9-]*\$"

    const val COGNITO_ROLE_KEY = "cognito:roles"
    const val COGNITO_USERNAME_KEY = "cognito:username"

}
