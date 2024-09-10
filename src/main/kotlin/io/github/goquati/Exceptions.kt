package io.github.goquati

import de.smart.nexus.orchestrator.oas_model.ErrorResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class Web2PdfException(
    private val msg: String,
    private val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : Exception(msg) {
    private val errorResponse
        get() = ErrorResponseDto(status = status.value(), error = status.name, message = msg)
    val responseEntity get() = ResponseEntity(errorResponse, status)
}