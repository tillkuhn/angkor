package net.timafe.angkor.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;

@Configuration
@EnableWebSecurity
// @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class  SecurityConfig : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    public override fun configure(http: HttpSecurity) {
        http.cors()

        http.csrf().disable()

        http.authorizeRequests()

                // Free information for everbody
                .antMatchers("/api/auth-info").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/actuator/health").permitAll()

                // requires authenication
                .antMatchers("/authorize").authenticated()
                .antMatchers("/api/secure/**").authenticated()

                // requires specific roles, ROLE_ prefix is added automatically
                .antMatchers( "/api/v1/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/v1/places/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/v1/places/**").hasRole("USER")
                .antMatchers(HttpMethod.POST, "/api/v1/places/**").hasRole("USER")
                .and()

                // Configures authentication support using an OAuth 2.0 and/or OpenID Connect 1.0 Provider.
                .oauth2Login()
                .and()
                //  Configures OAuth 2.0 Client support.
                .oauth2Client()
    }
}
