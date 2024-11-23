package io.github.goquati.api

import de.smart.nexus.orchestrator.api.Web2pdfApi
import de.smart.nexus.orchestrator.oas_model.IsReadyConditionDto
import de.smart.nexus.orchestrator.oas_model.PdfPrintOptionsDto
import de.smart.nexus.orchestrator.oas_model.Web2PdfRequestDto
import io.github.goquati.LoggerDelegate
import io.github.goquati.LoggerDelegate.Companion.measureExecutionTime
import io.github.goquati.service.Web2PdfService
import io.github.goquati.service.Web2PdfService.Companion.host
import io.ktor.http.*
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
        acceptLanguage: String?,
        customCss: String?,
        condition: IsReadyConditionDto?,
        options: PdfPrintOptionsDto?,
        serverHttpRequest: ServerHttpRequest,
    ): ResponseEntity<Resource> = Web2PdfRequestDto(
        url = url,
        headers = serverHttpRequest.extractQueryParams(regexHeaderKey),
        cookies = serverHttpRequest.extractQueryParams(regexCookieKey),
        acceptLanguage = acceptLanguage,
        customCss = customCss,
        condition = condition,
        options = options
    ).toPdfResponse()

    override suspend fun postPdf(
        web2PdfRequestDto: Web2PdfRequestDto,
        serverHttpRequest: ServerHttpRequest,
    ): ResponseEntity<Resource> =
        web2PdfRequestDto.toPdfResponse()

    private suspend fun Web2PdfRequestDto.toPdfResponse(): ResponseEntity<Resource> =
        log.measureExecutionTime("generated PDF ($host)") {
            web2PdfService.generatePdf(
                url = runCatching { Url(url) }.getOrElse { throw Exception("invalid url") },
                headers = headers,
                cookies = cookies,
                acceptLanguage = acceptLanguage,
                customCss = customCss,
                condition = condition,
                options = options,
            )
        }.let { ResponseEntity.ok(ByteArrayResource(it)) }

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
