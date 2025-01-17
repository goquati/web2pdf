package io.github.goquati

import io.github.goquati.oas.oas_model.ErrorResponseDto
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


@SpringBootApplication
@EnableConfigurationProperties
class Web2PdfApplication


@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(Web2PdfException::class)
    fun handleGeneratePdfException(
        ex: Web2PdfException,
        exchange: ServerWebExchange,
    ): Mono<ResponseEntity<ErrorResponseDto>> = Mono.just(ex.responseEntity)
}

fun main(args: Array<String>) {
    runApplication<Web2PdfApplication>(*args)
}
