package io.github.goquati.service

import de.smart.nexus.orchestrator.oas_model.PdfPrintOptionsDto
import io.github.goquati.Web2PdfException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class Html2PdfService(
    private val web2PdfService: Web2PdfService,
    private val environmentService: EnvironmentService,
) {
    private val htmlCache = MutableStateFlow<MutableMap<UUID, String>>(mutableMapOf())

    suspend fun generatePdf(
        data: String,
        options: PdfPrintOptionsDto?,
    ): ByteArray {
        val id = UUID.randomUUID()!!
        htmlCache.update { it[id] = data; it }
        try {
            return web2PdfService.generatePdf(
                url = environmentService.getLocalServerUrl("html2pdf-data/$id"),
                options = options,
            )
        } finally {
            htmlCache.update { it.remove(id); it }
        }
    }

    fun getHtml(id: UUID) = htmlCache.value[id]
        ?: throw Web2PdfException("html data not found", status = HttpStatus.NOT_FOUND)
}