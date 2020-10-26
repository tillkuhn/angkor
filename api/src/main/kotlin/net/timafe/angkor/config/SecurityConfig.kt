package net.timafe.angkor.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
// @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfig : WebSecurityConfigurerAdapter() {

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
                //.antMatchers("/management/**").hasAuthority(ADMIN)
                .and()
                .oauth2Login()
                .and()
                //.oauth2ResourceServer()
                //.jwt()
                //.jwtAuthenticationConverter(jwtAuthorityExtractor)
                //.and()
                .oauth2Client()
    }
}
