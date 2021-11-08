package net.timafe.angkor.config

/**
 * String Constants shared across all packages
 * use sparely and only if you need compile time values, otherwise prefer enums
 * If you have to, use object instead of companion objects
 *
 * See also Constants in Kotlin -- what's a recommended way to create them?
 * https://stackoverflow.com/a/50520935/4292075
 */
object Constants {

    const val API_ROOT = "/api"
    const val API_LATEST = "$API_ROOT/v1"
    const val API_PATH_ADMIN = "admin"

    const val PROFILE_CLEAN = "clean"
    const val PROFILE_PROD = "prod"
    const val PROFILE_TEST = "test"

    const val JPA_DEFAULT_RESULT_LIMIT = 199 // Default resultList Limit for JPA Queries,

    const val JACKSON_DATE_FORMAT = "yyyy-MM-dd"  /* should be "2019-11-08T07:08:45.134Z" */
    const val JACKSON_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX"  /* should be "2019-11-08T07:08:45.134+0200" */

    const val USER_SYSTEM = "00000000-0000-0000-0000-000000000001"

    // JsonFormat Constant is Deprecated, should now be taken care of nby objectMapper
    // JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]X")

}
