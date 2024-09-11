package io.github.goquati.api

import de.smart.nexus.orchestrator.api.Template2pdfApi
import de.smart.nexus.orchestrator.oas_model.TemplateInvoiceBaseDin5008Dto
import io.github.goquati.LoggerDelegate
import io.github.goquati.LoggerDelegate.Companion.measureExecutionTime
import io.github.goquati.service.Template2PdfService
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
class Template2PdfController(
    private val template2PdfService: Template2PdfService
) : Template2pdfApi {
    override suspend fun getInvoiceBaseDin5008(
        templateInvoiceBaseDin5008Dto: TemplateInvoiceBaseDin5008Dto,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<Resource> {
        return log.measureExecutionTime("generated PDF") {
            template2PdfService.generatePdf(
                data = templateInvoiceBaseDin5008Dto,
            )
        }.let { ResponseEntity.ok(ByteArrayResource(it)) }
    }

    @GetMapping("/template2pdf-data/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    suspend fun getHtml(@PathVariable id: UUID) = ResponseEntity.ok(template2PdfService.getHtml(id))

    companion object {
        private val log by LoggerDelegate()
    }
}