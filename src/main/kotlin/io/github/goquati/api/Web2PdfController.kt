package io.github.goquati.api

import io.github.goquati.LoggerDelegate
import io.github.goquati.LoggerDelegate.Companion.measureExecutionTime
import io.github.goquati.oas.api.Web2PdfApi
import io.github.goquati.oas.oas_model.*
import io.github.goquati.service.Html2PdfService
import io.github.goquati.service.Markdown2PdfService
import io.github.goquati.service.Template2PdfService
import io.github.goquati.service.Web2PdfService
import io.github.goquati.service.Web2PdfService.Companion.host
import io.ktor.http.*
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@Controller
class Web2PdfController(
    private val html2PdfService: Html2PdfService,
    private val markdown2PdfService: Markdown2PdfService,
    private val template2PdfService: Template2PdfService,
    private val web2PdfService: Web2PdfService,
) : Web2PdfApi {

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/web2pdf"],
        produces = ["application/pdf", "application/json"]
    )
    suspend fun getPdf(
        @Parameter @RequestParam(value = "url", required = true) url: String,
        @Parameter @RequestParam(value = "header", required = false) header: Map<String, String>?,
        @Parameter @RequestParam(value = "cookie", required = false) cookie: Map<String, String>?,
        @Parameter @RequestParam(value = "acceptLanguage", required = false) acceptLanguage: String?,
        @Parameter @RequestParam(value = "customCss", required = false) customCss: String?,
        @Parameter condition: IsReadyConditionDto?,
        @Parameter options: PdfPrintOptionsDto?, serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<Resource> = Web2PdfRequestDto(
        data = Web2PdfDto(
            url = url,
            headers = serverHttpRequest.extractQueryParams(regexHeaderKey),
            cookies = serverHttpRequest.extractQueryParams(regexCookieKey),
            acceptLanguage = acceptLanguage,
            customCss = customCss,
            condition = condition,
        ),
        options = options
    ).toPdfResponse()

    override suspend fun convertHtml2Pdf(
        html2PdfRequestDto: Html2PdfRequestDto,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<Resource> = log.measureExecutionTime("generated PDF") {
        html2PdfService.generatePdf(
            data = html2PdfRequestDto.data,
            options = html2PdfRequestDto.options,
        )
    }.let { ResponseEntity.ok(ByteArrayResource(it)) }

    override suspend fun convertMarkdown2Pdf(
        markdown2PdfRequestDto: Markdown2PdfRequestDto,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<Resource> = log.measureExecutionTime("generated PDF") {
        markdown2PdfService.generatePdf(
            data = markdown2PdfRequestDto.data,
            options = markdown2PdfRequestDto.options,
        )
    }.let { ResponseEntity.ok(ByteArrayResource(it)) }

    override suspend fun getInvoiceBaseDin5008(
        templateInvoiceBaseDin5008Dto: TemplateInvoiceBaseDin5008Dto,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<Resource> = log.measureExecutionTime("generated PDF") {
        template2PdfService.generatePdf(data = templateInvoiceBaseDin5008Dto)
    }.let { ResponseEntity.ok(ByteArrayResource(it)) }

    override suspend fun convertWeb2Pdf(
        web2PdfRequestDto: Web2PdfRequestDto,
        serverHttpRequest: ServerHttpRequest,
    ): ResponseEntity<Resource> = web2PdfRequestDto.toPdfResponse()

    private suspend fun Web2PdfRequestDto.toPdfResponse(): ResponseEntity<Resource> =
        log.measureExecutionTime("generated PDF ($host)") {
            web2PdfService.generatePdf(
                url = runCatching { Url(data.url) }.getOrElse { throw Exception("invalid url") },
                headers = data.headers,
                cookies = data.cookies,
                acceptLanguage = data.acceptLanguage,
                customCss = data.customCss,
                condition = data.condition,
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
