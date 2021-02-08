package net.timafe.angkor.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl

@Configuration
@EnableWebSecurity
// @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    public override fun configure(http: HttpSecurity) {

        // Adds a {@link CorsFilter} to be used. If a bean by the name of corsFilter is
        http.cors()

        http.csrf().disable()

        http.sessionManagement()

            // Controls the maximum number of sessions for a user. The default is to allow any
            .maximumSessions(1)
            // https://www.baeldung.com/spring-security-track-logged-in-users#alternative-method-using-sessionregistry
            .sessionRegistry(sessionRegistry())

        http.authorizeRequests()

                // Free information for everybody
                // .antMatchers("/api/auth-info").permitAll()
                // .antMatchers("/api/public/**").permitAll()
                .antMatchers("/actuator/health").permitAll()

                // requires authentication
                .antMatchers("/authorize").authenticated()
                // .antMatchers("/api/secure/**").authenticated()
                 .antMatchers("${Constants.API_LATEST}/user-summaries").authenticated()

                // requires specific roles, ROLE_ prefix is added automatically by hasRole()
                .antMatchers("${Constants.API_LATEST}/admin/**").hasRole("ADMIN")
                // * spread operator converts array into ...varargs
                .antMatchers(HttpMethod.DELETE, *getEntityPatterns()).hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, *getEntityPatterns()).hasRole("USER")
                .antMatchers(HttpMethod.PUT, *getEntityPatterns()).hasRole("USER")
                .and()

                // Configures authentication support using an OAuth 2.0 and/or OpenID Connect 1.0 Provider.
                // and Configures OAuth 2.0 Client support.
                .oauth2Login()
                    // specifies where users will be redirected after authenticating successfully (default /)
                    .defaultSuccessUrl("/home") // protected by HildeGuard :-)
                .and()
                .oauth2Client()
    }

    @Bean
    fun sessionRegistry(): SessionRegistry? {
        return SessionRegistryImpl()
    }

    fun getEntityPatterns(): Array<String> {
        return arrayOf("${Constants.API_LATEST}/places/**",
                "${Constants.API_LATEST}/notes/**",
                "${Constants.API_LATEST}/dishes/**",
                "${Constants.API_LATEST}/areas/**")
    }

}
