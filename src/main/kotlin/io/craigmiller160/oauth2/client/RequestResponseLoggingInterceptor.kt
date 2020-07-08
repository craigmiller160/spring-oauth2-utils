package io.craigmiller160.oauth2.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

class RequestResponseLoggingInterceptor : ClientHttpRequestInterceptor {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        logResponse(response)
        return response
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        val uri = request.uri
        val method = request.method
        val bodyString = String(body, StandardCharsets.UTF_8)
        log.debug("Request: $method $uri $bodyString")
    }

    private fun logResponse(response: ClientHttpResponse) {
        val status = response.statusCode
        val body = StreamUtils.copyToString(response.body, StandardCharsets.UTF_8)
        log.debug("Response $status $body")
    }
}

