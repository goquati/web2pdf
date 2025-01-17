package io.github.goquati.api

import io.github.goquati.service.Html2PdfService
import io.github.goquati.service.Template2PdfService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@Controller
class ResourceController(
    private val html2PdfService: Html2PdfService,
    private val template2PdfService: Template2PdfService
) {
    @GetMapping("/html2pdf-data/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    suspend fun getHtmlData(@PathVariable id: UUID): ResponseEntity<String> =
        ResponseEntity.ok(html2PdfService.getHtml(id))

    @GetMapping("/template2pdf-data/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    suspend fun getTemplateData(@PathVariable id: UUID): ResponseEntity<String> =
        ResponseEntity.ok(template2PdfService.getHtml(id))
}