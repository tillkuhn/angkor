package net.timafe.angkor

import org.slf4j.LoggerFactory
import org.springframework.test.context.ActiveProfilesResolver
import org.springframework.test.context.support.DefaultActiveProfilesResolver


/**
 * Based on https://www.allprogrammingtutorials.com/tutorials/overriding-active-profile-boot-integration-tests.php
 *
 * Allows you to override ActiveProfiles Annotation with
 * SPRING_PROFILES_ACTIVE=test,whatever
 */
class SytemEnvVarActiveProfileResolver : ActiveProfilesResolver {

    private val defaultActiveProfilesResolver = DefaultActiveProfilesResolver()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun resolve(testClass: Class<*>): Array<String> {
        //val springProfileKey = "spring.profiles.active"
        val envKey = "SPRING_PROFILES_ACTIVE"
        val envVar = System.getenv(envKey)
        return if (envVar != null) {
            this.log.info("Overwrite with env key $envKey = $envVar")
            getProps(envVar)
        } else defaultActiveProfilesResolver.resolve(testClass)
    }

    fun getProps(str: String): Array<String> {
        val props = str
                .split("\\s*,\\s*"
                .toRegex()).toTypedArray()
        return props
    }
}
