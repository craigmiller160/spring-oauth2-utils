package io.craigmiller160.spring.oauth2.client

import io.craigmiller160.oauth2.client.AuthServerClientRequest
import io.craigmiller160.oauth2.dto.TokenResponseDto
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

val buildExecuteRequest = { template: RestTemplate -> { request: AuthServerClientRequest ->
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
    headers.setBasicAuth(request.clientKey, request.clientSecret)

    val body = LinkedMultiValueMap<String,String>()
    request.body.map { entry -> body.add(entry.key, entry.value) }

    val entity = HttpEntity<MultiValueMap<String,String>>(body, headers)

    template.exchange(request.url, HttpMethod.POST, entity, TokenResponseDto::class.java).body
} }