package io.github.klahap.api

import de.smart.nexus.orchestrator.api.Web2pdfApi
import de.smart.nexus.orchestrator.oas_model.PdfPrintOptionsDto
import de.smart.nexus.orchestrator.oas_model.Web2PdfRequestDto
import io.github.klahap.LoggerDelegate
import io.github.klahap.LoggerDelegate.Companion.measureExecutionTime
import io.github.klahap.service.Web2PdfService
import io.github.klahap.service.Web2PdfService.Companion.host
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller

@Controller
class Web2PdfController(
    private val web2PdfService: Web2PdfService,
) : Web2pdfApi {
    override suspend fun getPdf(
        url: String,
        header: Map<String, String>?,
        cookie: Map<String, String>?,
        options: PdfPrintOptionsDto?,
        serverHttpRequest: ServerHttpRequest,
    ): ResponseEntity<Resource> = Web2PdfRequestDto(
        url = url,
        headers = serverHttpRequest.extractQueryParams(regexHeaderKey),
        cookies = serverHttpRequest.extractQueryParams(regexCookieKey),
        options = options
    ).toPdfResponse()

    override suspend fun postPdf(
        web2PdfRequestDto: Web2PdfRequestDto,
        serverHttpRequest: ServerHttpRequest,
    ): ResponseEntity<Resource> =
        web2PdfRequestDto.toPdfResponse()

    private suspend fun Web2PdfRequestDto.toPdfResponse(): ResponseEntity<Resource> =
        log.measureExecutionTime("generated PDF ($host)") {
            web2PdfService.generatePdf(this)
        }.let { ResponseEntity.ok<Resource>(ByteArrayResource(it)) }

    companion object {
        private val log by LoggerDelegate()

        private val regexHeaderKey = Regex("^header\\[([^]]*)]$")
        private val regexCookieKey = Regex("^cookie\\[([^]]*)]$")

        private fun ServerHttpRequest.extractQueryParams(keyRegex: Regex) = queryParams.mapNotNull {
            val key = keyRegex.matchEntire(it.key)?.groups?.get(1)?.value ?: return@mapNotNull null
            val value = it.value.singleOrNull() ?: return@mapNotNull null
            key to value
        }.toMap()
    }
}