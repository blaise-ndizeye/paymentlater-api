package com.blaise.paymentlater.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Asynchronous processing configuration for non-blocking operations.
 * 
 * Configures thread pool executor for handling async tasks like:
 * - Email notifications
 * - Webhook deliveries
 * - Event processing
 * 
 * **Thread Pool Settings**:
 * - Core Pool: 5 threads (always kept alive)
 * - Max Pool: 20 threads (scales up under load)
 * - Queue Capacity: 50 pending tasks
 * - Thread naming: "Async-" prefix for debugging
 */
@Configuration
class AsyncConfig {

    /**
     * Task executor bean for asynchronous method processing.
     * 
     * Used by @Async annotated methods in event listeners and
     * notification services to improve API response times.
     */
    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 20
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("Async-")
        executor.initialize()
        return executor
    }
}