package com.blaise.paymentlater.config

import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

/**
 * Reactive HTTP client configuration for external service communication.
 * 
 * This configuration provides a pre-configured WebClient bean for making
 * non-blocking HTTP requests to external services, with optimized timeout
 * settings for reliable communication.
 * 
 * **WebClient Features**:
 * - Reactive, non-blocking HTTP client
 * - Built on Project Reactor and Netty
 * - Support for reactive streams (Mono/Flux)
 * - Automatic JSON serialization/deserialization
 * - Connection pooling and keep-alive
 * 
 * **Timeout Configuration**:
 * - **Response Timeout**: 5 seconds maximum wait for response
 * - **Connection Timeout**: 5 seconds maximum wait for connection establishment
 * - Prevents hanging requests and resource exhaustion
 * - Provides predictable performance characteristics
 * 
 * **Use Cases**:
 * - Webhook delivery to merchant endpoints
 * - External payment gateway integration
 * - Third-party service API calls
 * - Microservice communication
 * - Email service provider integration
 * 
 * **Performance Benefits**:
 * - Non-blocking I/O allows handling many concurrent requests
 * - Connection reuse reduces overhead
 * - Reactive streams provide backpressure handling
 * - Memory efficient compared to traditional blocking clients
 * 
 * **Error Handling**:
 * - Timeout exceptions for slow responses
 * - Connection exceptions for network issues
 * - Reactive error propagation through Mono/Flux
 * - Retry capabilities can be added at service layer
 * 
 * @see MailService
 * @see org.springframework.web.reactive.function.client.WebClient
 */
@Configuration
class WebClientConfig {

    /**
     * Creates a configured WebClient bean for reactive HTTP operations.
     * 
     * Builds a WebClient instance with custom timeout settings and Netty-based
     * HTTP client for optimal performance and reliability in external service
     * communication.
     * 
     * **HTTP Client Configuration**:
     * - Uses Reactor Netty for non-blocking I/O
     * - Response timeout: 5 seconds (prevents hanging requests)
     * - Connection timeout: 5,000ms (fast connection establishment)
     * - Connection pooling enabled by default
     * 
     * **Timeout Behavior**:
     * - **Response Timeout**: Total time to receive complete response
     * - **Connection Timeout**: Time to establish TCP connection
     * - Both timeouts prevent resource exhaustion
     * - Throws TimeoutException when limits exceeded
     * 
     * **Usage Examples**:
     * ```kotlin
     * // GET request
     * webClient.get()
     *     .uri("https://api.example.com/data")
     *     .retrieve()
     *     .bodyToMono(String::class.java)
     * 
     * // POST request with JSON body
     * webClient.post()
     *     .uri("https://webhook.merchant.com/notify")
     *     .contentType(MediaType.APPLICATION_JSON)
     *     .bodyValue(webhookData)
     *     .retrieve()
     *     .bodyToMono(String::class.java)
     * ```
     * 
     * **Bean Scope**: Singleton - safe for concurrent use across application
     * 
     * @return Configured WebClient instance ready for reactive HTTP operations
     */
    @Bean
    fun webClient(): WebClient {
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(5))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}