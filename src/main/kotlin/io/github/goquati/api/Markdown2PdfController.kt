package io.github.goquati.api

import de.smart.nexus.orchestrator.api.Markdown2pdfApi
import de.smart.nexus.orchestrator.oas_model.Markdown2PdfRequestDto
import io.github.goquati.LoggerDelegate
import io.github.goquati.LoggerDelegate.Companion.measureExecutionTime
import io.github.goquati.service.Markdown2PdfService
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller

@Controller
class Markdown2PdfController(
    private val markdown2PdfService: Markdown2PdfService,
) : Markdown2pdfApi {
    override suspend fun convertMarkdown2Pdf(
        markdown2PdfRequestDto: Markdown2PdfRequestDto,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<Resource> {
        return log.measureExecutionTime("generated PDF") {
            markdown2PdfService.generatePdf(
                data = markdown2PdfRequestDto.data,
                options = markdown2PdfRequestDto.options,
            )
        }.let { ResponseEntity.ok(ByteArrayResource(it)) }
    }

    companion object {
        private val log by LoggerDelegate()
    }
}