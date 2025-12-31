package net.timafe.angkor.config

import net.timafe.angkor.domain.enums.EntityType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint


/**
 * Configure Spring Security
 *
 * Simple Kotlin Example:
 * https://github.com/spring-projects/spring-security-samples/blob/main/servlet/spring-boot/kotlin/hello-security/src/main/kotlin/org/springframework/security/samples/config/SecurityConfig.kt
 *
 * 2024: Check out Advanced Spring Security - How to create multiple Spring Security Configurations
 * - https://www.danvega.dev/blog/multiple-spring-security-configs
 *
 * Spring Security: Multiple HttpSecurity Instances
 * - https://docs.spring.io/spring-security/reference/servlet/configuration/java.html#_multiple_httpsecurity_instances
 * -- Create an instance of SecurityFilterChain that contains @Order to specify which SecurityFilterChain should be considered first.
 * -- securityMatcher("/api/ * * ") states that this HttpSecurity is applicable only to URLs that start with /api/.
 * -- Create another instance of SecurityFilterChain. If the URL does not start with /api/, this configuration is used.
 * 	 This configuration is considered after apiFilterChain, since it has an @Order value after 1 (no @Order defaults to last).
 *
 * deprecated authorizeRequests() vs. new authorizeHttpRequests:
 * * https://stackoverflow.com/questions/73089730/authorizerequests-vs-authorizehttprequestscustomizerauthorizehttprequestsc
 * * https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
 *
 * Get rid of WebSecurityConfigurerAdapter (overrides configure(http: HttpSecurity)
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(private val basicAuthProvider: BasicAuthenticationProvider) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    // Special filter chain for basic auth use cases, e.g. prometheus endpoint protection
    @Bean
    @Order(1) // serve me first, please
    @Throws(Exception::class)
    fun basicAuthFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/actuator/prometheus")
            .authorizeHttpRequests {
                // it.anyRequest().authenticated()
                //it.anyRequest().authenticated()
                // 2024-09 try stateless? https://javadevjournal.com/spring-security/spring-security-session/
                it.requestMatchers(HttpMethod.GET, ("/actuator/prometheus")).authenticated()
            }

            // Avoid session: https://www.javadevjournal.com/spring-security/spring-security-session/
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.NEVER) }
            // https://dzone.com/articles/java-spring-oauth2-and-basic-auth-support
            .authenticationProvider(basicAuthProvider)
            .httpBasic(Customizer.withDefaults())
            //.formLogin{it.disable()}
            //.oauth2Login{it.disable()}

            // by default Spring will redirect (301) to login page but for basic auth we
            // want a straight 403 (otherwise for instance Grafana Cloud Auth test won't work
            // since tests the endpoint without and expects a straight denial, and does not support redirects)
            // DOES NOT WORK :-(
            .exceptionHandling {
                it.authenticationEntryPoint(Http403ForbiddenEntryPoint())
            }
            .csrf { it.disable() }
        // it.authenticationDetailsSource { metricUsers() }
        return http.build()
    }

    // default filter chain with OAuth2 authentication
    @Bean
    // No order (evaluated last, no securityMatcher
    @Throws(java.lang.Exception::class)
    fun defaultFilterChain(http: HttpSecurity): SecurityFilterChain? {
        log.debug("init SecurityFilterChain for {}", http)

        // Adds a {@link CorsFilter} to be used
        return http
            .cors(Customizer.withDefaults())

            .csrf { it.disable() }

            .sessionManagement {
                // Controls the maximum number of sessions for a user. The default is to allow any
                // Registry: https://www.baeldung.com/spring-security-track-logged-in-users#alternative-method-using-sessionregistry
                it.maximumSessions(1).sessionRegistry(sessionRegistry())
            }

            // authorizeRequests is deprecated, but authorizeHttpRequests always returns 403
            .authorizeHttpRequests {
                it
                    // Free routes for everybody
                    // default is authenticated, so permitAll resources  have to be declared explicitly
                    .requestMatchers("/actuator/health/**").permitAll()
                    .requestMatchers(HttpMethod.GET, ("${Constants.API_LATEST}/stats")).permitAll()
                    .requestMatchers(HttpMethod.GET, ("${Constants.API_LATEST}/public/**")).permitAll()

                    // Auth related methods that have to be open for anonymous access
                    .requestMatchers(HttpMethod.GET, ("${Constants.API_LATEST}/authenticated")).permitAll()
                    .requestMatchers(HttpMethod.GET, ("${Constants.API_LATEST}/authentication")).permitAll()
                    .requestMatchers(HttpMethod.GET, ("/oauth2/authorization/**")).permitAll()

                    // requires admin (any method)
                    .requestMatchers("${Constants.API_LATEST}/admin/**").hasRole("ADMIN")

                    // Entity Path Setup requires specific roles, ROLE_ prefix is added automatically by hasRole()
                    // Tip: * spread operator converts array into ...varargs
                    // Allow POST search for all entities (pois and locations are not entities, so we need to
                    // add them explicitly, all other entity names are covered by getEntityPatterns)
                    .requestMatchers(HttpMethod.GET, ("${Constants.API_LATEST}/pois/**")).permitAll()
                    .requestMatchers(HttpMethod.GET, ("${Constants.API_LATEST}/locations/**")).permitAll()
                    .requestMatchers(HttpMethod.POST, ("${Constants.API_LATEST}/locations/search")).permitAll()
                    .requestMatchers(HttpMethod.POST, *getEntityPatterns("/search")).permitAll()
                    .requestMatchers(HttpMethod.GET, *getEntityPatterns("/**")).permitAll() // GET is public
                    // restricted entity rules
                    .requestMatchers(HttpMethod.DELETE, *getEntityPatterns("/**")).hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, *getEntityPatterns("/**")).hasRole("USER")
                    .requestMatchers(HttpMethod.PUT, *getEntityPatterns("/**")).hasRole("USER")

                    // default deny (but maybe not necessary, as it's implicit ??)
                    //  requires authentication (any role) necessary ??
                    // .requestMatchers("/authorize").authenticated()
                    // .requestMatchers("${Constants.API_LATEST}/user-summaries").authenticated()
                    .anyRequest().authenticated()
            }

            // Configures authentication support using an OAuth 2.0 and/or OpenID Connect 1.0 Provider.
            .oauth2Login {
                // defaultSuccessUrl specifies where users will be redirected after authenticating successfully (default /)
                it.defaultSuccessUrl("/home") /* protected by HildeGuard :-) */
            }
            .oauth2Client(Customizer.withDefaults())
            .build()
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
