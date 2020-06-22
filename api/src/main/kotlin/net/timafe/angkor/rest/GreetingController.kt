package net.timafe.angkor.rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import net.timafe.angkor.domain.Greeting
import java.util.concurrent.atomic.AtomicLong

@RestController
class GreetingController {

    val counter = AtomicLong()
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String) =
            Greeting(counter.incrementAndGet(), "Hello, $name")

}
