package io.github.goquati.service

import de.smart.nexus.orchestrator.oas_model.PdfPrintOptionsDto
import de.smart.nexus.orchestrator.oas_model.Web2PdfRequestDto
import io.github.goquati.Web2PdfException
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.hildan.chrome.devtools.domains.network.CookieParam
import org.hildan.chrome.devtools.domains.network.NetworkDomain
import org.hildan.chrome.devtools.domains.page.PrintToPDFRequest
import org.hildan.chrome.devtools.protocol.ExperimentalChromeApi
import org.hildan.chrome.devtools.sessions.goto
import org.hildan.chrome.devtools.sessions.use
import org.springframework.stereotype.Service
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Service
class Web2PdfService(
    private val browserSessionService: BrowserSessionService,
) {
    suspend fun generatePdf(
        url: String,
        headers: Map<String, String>? = null,
        cookies: Map<String, String>? = null,
        options: PdfPrintOptionsDto?,
    ): ByteArray = runCatching {
        val url = runCatching { Url(url) }.getOrElse { throw Exception("invalid url") }
        browserSessionService.getPageSession().use { pageSession ->
            pageSession.network.addHeaders(headers ?: emptyMap())
            pageSession.network.addCookies(cookies ?: emptyMap(), host = url.host)
            runCatching {
                pageSession.goto(url.toString())
            }.getOrElse { throw Exception("Navigation to '${url.host}' failed. The full URL is hidden for security reasons. Please ensure the URL is correct and reachable.") }

            pageSession.page.printToPDF(options?.pdfOptions ?: PrintToPDFRequest())
                .let { @OptIn(ExperimentalEncodingApi::class) Base64.decode(it.data) }
        }
    }.getOrElse { throw Web2PdfException(it.message ?: "") }


    private suspend fun NetworkDomain.addHeaders(data: Map<String, String>) {
        if (data.isEmpty()) return
        enable { }
        setExtraHTTPHeaders(
            JsonObject(data.mapValues { JsonPrimitive(it.value) })
        )
    }

    private suspend fun NetworkDomain.addCookies(data: Map<String, String>, host: String) {
        if (data.isEmpty()) return
        enable { }
        setCookies(
            data.map {
                @OptIn(ExperimentalChromeApi::class) (CookieParam(
                    name = it.key,
                    value = it.value,
                    domain = host
                ))
            }
        )
    }


    companion object {
        val Web2PdfRequestDto.host get() = Url(url).host

        private fun String.checkNullableStringValue(name: String) {
            if (this == "null") throw Exception("invalid value 'null' for '$name'")
        }

        private val PdfPrintOptionsDto.pdfOptions
            get() = PrintToPDFRequest(
                landscape = landscape,
                displayHeaderFooter = displayHeaderFooter,
                printBackground = printBackground,
                scale = scale,
                paperWidth = paperWidth,
                paperHeight = paperHeight,
                marginTop = marginTop,
                marginBottom = marginBottom,
                marginLeft = marginLeft,
                marginRight = marginRight,
                pageRanges = pageRanges?.apply { checkNullableStringValue("pageRanges") },
                headerTemplate = headerTemplate?.apply { checkNullableStringValue("headerTemplate") },
                footerTemplate = footerTemplate?.apply { checkNullableStringValue("footerTemplate") },
                preferCSSPageSize = preferCSSPageSize,
                generateTaggedPDF = generateTaggedPDF,
                generateDocumentOutline = generateDocumentOutline,
            )
    }
}