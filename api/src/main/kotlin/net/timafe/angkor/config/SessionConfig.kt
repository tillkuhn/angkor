package net.timafe.angkor.config

import net.timafe.angkor.service.SessionListener
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *  https://www.baeldung.com/httpsessionlistener_with_metrics
 */
@Configuration
class SessionConfig {

    @Bean
    fun sessionListener(): ServletListenerRegistrationBean<SessionListener>? {
        val listenerRegBean: ServletListenerRegistrationBean<SessionListener> =
            ServletListenerRegistrationBean()
        listenerRegBean.listener = SessionListener()
        return listenerRegBean
    }
}
