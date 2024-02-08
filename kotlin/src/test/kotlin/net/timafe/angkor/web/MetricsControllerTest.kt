package net.timafe.angkor.web

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class MetricsControllerTest(private val controller: MetricsController) {

    fun `test itemCount stats`() {
        val stats = controller.entityStats()
        Assertions.assertThat(stats["places"]).isGreaterThan(0)
        Assertions.assertThat(stats["notes"]).isGreaterThan(0)
        Assertions.assertThat(stats["pois"]).isGreaterThan(0)
        Assertions.assertThat(stats["dishes"]).isGreaterThan(0)
        Assertions.assertThat(stats["tours"]).isGreaterThan(0)
        Assertions.assertThat(stats["posts"]).isGreaterThan(0)
        Assertions.assertThat(stats["videos"]).isGreaterThan(0)
        Assertions.assertThat(stats["photos"]).isGreaterThan(0)
    }

    fun `test admin status`() {
        val stats = controller.metrics()
        // Size should all custom metrics registered in MetricsController
        // plus those that are not ignored in net.timafe.angkor.config.MetricsConfig
        Assertions.assertThat(stats.size).isGreaterThan(15)
    }

}
