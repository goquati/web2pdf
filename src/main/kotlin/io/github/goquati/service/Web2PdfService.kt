package io.github.goquati.service

import de.smart.nexus.orchestrator.oas_model.IsReadyConditionDto
import de.smart.nexus.orchestrator.oas_model.PdfPrintOptionsDto
import de.smart.nexus.orchestrator.oas_model.Web2PdfRequestDto
import io.github.goquati.Web2PdfException
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import org.hildan.chrome.devtools.domains.emulation.EmulationDomain
import org.hildan.chrome.devtools.domains.emulation.SetUserAgentOverrideRequest
import org.hildan.chrome.devtools.domains.network.CookieParam
import org.hildan.chrome.devtools.domains.network.NetworkDomain
import org.hildan.chrome.devtools.domains.page.PrintToPDFRequest
import org.hildan.chrome.devtools.domains.runtime.RuntimeDomain
import org.hildan.chrome.devtools.protocol.ExperimentalChromeApi
import org.hildan.chrome.devtools.sessions.PageSession
import org.hildan.chrome.devtools.sessions.goto
import org.hildan.chrome.devtools.sessions.use
import org.intellij.lang.annotations.Language
import org.springframework.stereotype.Service
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Service
class Web2PdfService(
    private val browserSessionService: BrowserSessionService,
) {
    suspend fun generatePdf(
        url: Url,
        headers: Map<String, String>? = null,
        cookies: Map<String, String>? = null,
        @Language("css") customCss: String? = null,
        condition: IsReadyConditionDto? = null,
        acceptLanguage: String? = null,
        options: PdfPrintOptionsDto?,
    ): ByteArray = runCatching {
        browserSessionService.getPageSession().use { pageSession ->
            pageSession.network.apply {
                addHeaders(headers ?: emptyMap())
                addCookies(cookies ?: emptyMap(), host = url.host)
            }
            if (acceptLanguage != null)
                pageSession.emulation.setAcceptLanguage(acceptLanguage)

            runCatching {
                pageSession.goto(url.toString())
            }.getOrElse { error("Navigation to '${url.host}' failed. The full URL is hidden for security reasons. Please ensure the URL is correct and reachable.") }

            if (customCss != null)
                pageSession.setCustomCss(customCss)
            if (condition != null)
                pageSession.runtime.waitForReady(condition)

            pageSession.page.printToPDF(options?.pdfOptions ?: PrintToPDFRequest())
                .let { @OptIn(ExperimentalEncodingApi::class) Base64.decode(it.data) }
        }
    }.getOrElse { throw Web2PdfException(it.message ?: "") }

    @OptIn(ExperimentalChromeApi::class)
    private suspend fun PageSession.setCustomCss(data: String) {
        dom.enable()
        val frameId = page.getFrameTree().frameTree.frame.id
        css.apply {
            enable()
            val styleSheetId = createStyleSheet(frameId = frameId).styleSheetId
            setStyleSheetText(styleSheetId = styleSheetId, text = data)
        }
    }

    @OptIn(ExperimentalChromeApi::class)
    private suspend fun EmulationDomain.setAcceptLanguage(language: String) {
        setUserAgentOverride(
            SetUserAgentOverrideRequest(
                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36",
                acceptLanguage = language
            )
        )
    }

    private suspend fun RuntimeDomain.waitForReady(condition: IsReadyConditionDto) {
        enable()
        var isLoaded = false
        for (i in 0..<condition.maxTries) {
            isLoaded = evaluate(expression = condition.expression)
                .result.value?.let { it as? JsonPrimitive }?.booleanOrNull
                ?: error("invalid response of custom JS condition")
            if (isLoaded) break
            delay(condition.delayInMilliSeconds.toLong())
        }
        if (!isLoaded)
            error("timeout, custom JS condition not full filled")
    }

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