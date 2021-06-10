package net.timafe.angkor.service

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener


/**
 * Keep track of created http sessions
 * https://www.baeldung.com/httpsessionlistener_with_metrics
 */
class SessionListener : HttpSessionListener {

    private val log = LoggerFactory.getLogger(javaClass)
    private val activeSessions = AtomicInteger()

    fun getTotalActiveSession(): Int {
        return activeSessions.get()
    }

    override fun sessionCreated(event: HttpSessionEvent?) {
        // Returns the maximum time interval, in seconds, that the servlet container
        //     * will keep this session open between client accesses. After this interval,
        //     * the servlet container will invalidate the session.
        // see also application.yaml server.servlet.session.timeout
        val maxInactive = event!!.session!!.maxInactiveInterval
        log.info("[SessionTracker] Create new session, currentCount=${activeSessions.incrementAndGet()} maxInactiveInterval=${maxInactive / 60}m")
    }

    override fun sessionDestroyed(event: HttpSessionEvent?) {
        log.info("Destroy session, newCount=${activeSessions.decrementAndGet()}")
    }

}
