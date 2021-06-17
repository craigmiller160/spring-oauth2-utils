/*
 * oauth2-utils
 * Copyright (C) 2020 Craig Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.spring.oauth2.client

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
        log.debug("RestTemplate Request: $method $uri $bodyString")
    }

    private fun logResponse(response: ClientHttpResponse) {
        val status = response.statusCode
//        val body = StreamUtils.copyToString(response.body, StandardCharsets.UTF_8)
//        log.debug("Response $status $body")
        log.debug("RestTemplate Response $status")
    }
}

