package net.timafe.angkor.config

import net.timafe.angkor.domain.enums.EntityType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.SecurityFilterChain


/**
 * Main configuration for the http security filter chain
 *
 * Get rid of WebSecurityConfigurerAdapter (overrides configure(http: HttpSecurity)
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sample https://github.com/spring-projects/spring-security-samples/blob/main/servlet/spring-boot/kotlin/hello-security/src/main/kotlin/org/springframework/security/samples/config/SecurityConfig.kt
     *
     * 2024: Check out Advanced Spring Security - How to create multiple Spring Security Configurations
     * https://www.danvega.dev/blog/multiple-spring-security-configs
     */
    @Bean
    @Throws(java.lang.Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {

        // Adds a {@link CorsFilter} to be used
        // Deprecated For removal in 7.0. Use cors(Customizer) or cors(Customizer.withDefaults())  (same with csrf)
        http.cors(Customizer.withDefaults())

        http.csrf { it.disable() }

        http.sessionManagement {
            // Controls the maximum number of sessions for a user. The default is to allow any
            // Registry: https://www.baeldung.com/spring-security-track-logged-in-users#alternative-method-using-sessionregistry
            it.maximumSessions(1).sessionRegistry(sessionRegistry())
        }

        //            // Controls the maximum number of sessions for a user. The default is to allow any
        //            .maximumSessions(1)
        //            // https://www.baeldung.com/spring-security-track-logged-in-users#alternative-method-using-sessionregistry
        //            .sessionRegistry(sessionRegistry())

        // authorizeRequests is deprecated, but authorizeHttpRequests always returns 403
        http.authorizeRequests()

            // Free information for everybody
            .requestMatchers("/actuator/health/**").permitAll()

            // Allow POST search for all entities
            .requestMatchers(HttpMethod.POST, *getEntityPatterns("/search")).permitAll()

            // requires authentication (any role)
            .requestMatchers("/authorize").authenticated()
            .requestMatchers("${Constants.API_LATEST}/user-summaries").authenticated()

            // requires specific roles, ROLE_ prefix is added automatically by hasRole()
            // Tip: * spread operator converts array into ...varargs
            .requestMatchers("${Constants.API_LATEST}/admin/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, *getEntityPatterns("/**")).hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, *getEntityPatterns("/**")).hasRole("USER")
            .requestMatchers(HttpMethod.PUT, *getEntityPatterns("/**")).hasRole("USER")

            // Free information for everybody
            // // .antMatchers("/api/auth-info").permitAll()
            // // .antMatchers("/api/public/**").permitAll()
            // .antMatchers("/actuator/health").permitAll()

            // Allow POST search for all entities
            //.antMatchers(HttpMethod.POST, *getEntityPatterns("/search")).permitAll() // only allow search

            // requires authentication (any role)
            //.antMatchers("/authorize").authenticated()
            //.antMatchers("${Constants.API_LATEST}/user-summaries").authenticated()

            // requires specific roles, ROLE_ prefix is added automatically by hasRole()
            // Tip: * spread operator converts array into ...varargs
            //.antMatchers("${Constants.API_LATEST}/admin/**").hasRole("ADMIN")
            // .antMatchers(HttpMethod.DELETE, *getEntityPatterns("/**")).hasRole("ADMIN")
            // .antMatchers(HttpMethod.POST, *getEntityPatterns("/**")).hasRole("USER")
            // .antMatchers(HttpMethod.PUT, *getEntityPatterns("/**")).hasRole("USER")

            .and()

            // Configures authentication support using an OAuth 2.0 and/or OpenID Connect 1.0 Provider.
            // and Configures OAuth 2.0 Client support.
            // defaultSuccessUrl specifies where users will be redirected after authenticating successfully (default /)
            .oauth2Login {
                it.defaultSuccessUrl("/home") /* protected by HildeGuard :-) */
            }
            // .defaultSuccessUrl("/home") // protected by HildeGuard :-)
            //.and()
            .oauth2Client(Customizer.withDefaults())
        log.info("init SecurityFilterChain for $http")
        return http.build()
    }

    /**
     * Returns an array of patterns for each known entity type (e.g. /api/places/suffix,/api/dishes/suffix ... )
     * Main usecase is to quickly setup antMatchers security rules that apply to all entities
     */
    fun getEntityPatterns(suffix: String): Array<String> {
        return EntityType.entries.map { "${Constants.API_LATEST}/${it.path}${suffix}" }.toTypedArray()
    }

    /**
     * SessionRegistry Maintains a registry of SessionInformation instances,
     * as we want to keep track of active sessions in the current admin section
     */
    @Bean
    fun sessionRegistry(): SessionRegistry? {
        return SessionRegistryImpl()
    }

}
