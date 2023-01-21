package net.timafe.angkor.web

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Custom Error Page instead of Whitelabel Error Page
 *
 * https://www.baeldung.com/spring-boot-custom-error-page
 */
@Controller
class ErrorController: ErrorController {

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest,response: HttpServletResponse) {
        //do something like logging
        val status: Any = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)

        when (val statusCode = Integer.valueOf(status.toString())) {
                HttpStatus.NOT_FOUND.value() -> {
                    response.writer.println("\uD83D\uDE1E Sorry, we still haven't found what you're looking for!")
                }
                HttpStatus.INTERNAL_SERVER_ERROR.value() -> {
                    response.writer.println("\uD83D\uDD25 Sorry so sorry, it's all the sever's fault!")
                }
                else -> {
                    response.writer.println("Oops, this was an unexpected status code: $statusCode")
                }
            }
    }
}

