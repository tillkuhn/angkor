package net.timafe.angkor.config

import net.timafe.angkor.domain.enums.EntityType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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

    /**
     * Sample https://github.com/spring-projects/spring-security-samples/blob/main/servlet/spring-boot/kotlin/hello-security/src/main/kotlin/org/springframework/security/samples/config/SecurityConfig.kt
     *
     */
    @Bean
    @Throws(java.lang.Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {

        // Adds a {@link CorsFilter} to be used
        http.cors()

        http.csrf().disable()

        http.sessionManagement()

            // Controls the maximum number of sessions for a user. The default is to allow any
            .maximumSessions(1)
            // https://www.baeldung.com/spring-security-track-logged-in-users#alternative-method-using-sessionregistry
            .sessionRegistry(sessionRegistry())

        http.authorizeHttpRequests()
            .requestMatchers("/actuator/health/**").permitAll()
            .requestMatchers(HttpMethod.POST, *getEntityPatterns("/search")).permitAll()
            .requestMatchers("/authorize").authenticated()
            .requestMatchers("${Constants.API_LATEST}/user-summaries").authenticated()
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
            .oauth2Login()
            // specifies where users will be redirected after authenticating successfully (default /)
            .defaultSuccessUrl("/home") // protected by HildeGuard :-)
            .and()
            .oauth2Client()
        return http.build()
    }

    /**
     * Returns an array of patterns for each known entity type (e.g. /api/places/suffix,/api/dishes/suffix ... )
     * Main usecase is to quickly setup antMatchers security rules that apply to all entities
     */
    fun getEntityPatterns(suffix: String): Array<String> {
        return EntityType.values().map { "${Constants.API_LATEST}/${it.path}${suffix}" }.toTypedArray()
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
