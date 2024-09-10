package io.github.goquati.api

import de.smart.nexus.orchestrator.api.Html2pdfApi
import de.smart.nexus.orchestrator.oas_model.Html2PdfRequestDto
import io.github.goquati.LoggerDelegate
import io.github.goquati.LoggerDelegate.Companion.measureExecutionTime
import io.github.goquati.service.Html2PdfService
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@Controller
class Html2PdfController(
    private val html2PdfService: Html2PdfService,
) : Html2pdfApi {
    override suspend fun convertHtml2Pdf(
        html2PdfRequestDto: Html2PdfRequestDto,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<Resource> {
        return log.measureExecutionTime("generated PDF") {
            html2PdfService.generatePdf(
                data = html2PdfRequestDto.data,
                options = html2PdfRequestDto.options,
            )
        }.let { ResponseEntity.ok(ByteArrayResource(it)) }
    }

    @GetMapping("/html2pdf-data/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    suspend fun getHtml(@PathVariable id: UUID) = ResponseEntity.ok(html2PdfService.getHtml(id))

    companion object {
        private val log by LoggerDelegate()
    }
}