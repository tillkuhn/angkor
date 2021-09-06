package net.timafe.angkor.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Configuration for async, based on JHipster bean except we don't use their
 * JHipster ExceptionHandlingAsyncTaskExecutor but
 * * https://www.javacodegeeks.com/2020/01/send-your-data-async-on-kafka.html
 * * https://www.baeldung.com/spring-async
 */
@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfig(private val taskExecutionProperties: TaskExecutionProperties) : AsyncConfigurer {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean(name = ["taskExecutor"])
    override fun getAsyncExecutor(): Executor {
        log.debug("Creating Async Task Executor prefix=${taskExecutionProperties.threadNamePrefix} coreSize=${taskExecutionProperties.pool.coreSize}")
        val executor = ThreadPoolTaskExecutor().apply {
            corePoolSize = taskExecutionProperties.pool.coreSize
            maxPoolSize = taskExecutionProperties.pool.maxSize
        }
        executor.setQueueCapacity(taskExecutionProperties.pool.queueCapacity)
        executor.setThreadNamePrefix( taskExecutionProperties.threadNamePrefix)
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler() = SimpleAsyncUncaughtExceptionHandler()
}
