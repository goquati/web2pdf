package io.github.goquati.web2pdf.dsl

import io.github.goquati.web2pdf.apis.Web2PdfApi
import io.github.goquati.web2pdf.models.*
import io.ktor.client.statement.*
import org.intellij.lang.annotations.Language


fun PdfPrintOptions.setSizeDinA4() {
    paperWidth = 8.27
    paperHeight = 11.69
}

fun PdfPrintOptions.setMargin(v: Double) {
    marginTop = v
    marginBottom = v
    marginLeft = v
    marginRight = v
}

suspend fun Web2PdfApi.convertHtml2Pdf(
    @Language("html") html: String,
    block: (PdfPrintOptions.() -> Unit)? = null,
): HttpResponse {
    val options = block?.let { PdfPrintOptions().apply(it) }
    return convertHtml2Pdf(Html2PdfRequest(html, options))
}

suspend fun Web2PdfApi.convertMarkdown2Pdf(
    @Language("markdown") markdown: String,
    block: (PdfPrintOptions.() -> Unit)? = null,
): HttpResponse {
    val options = block?.let { PdfPrintOptions().apply(it) }
    return convertMarkdown2Pdf(Markdown2PdfRequest(markdown, options))
}

suspend fun Web2PdfApi.convertWeb2Pdf(
    data: Web2Pdf,
    block: (PdfPrintOptions.() -> Unit)? = null,
): HttpResponse {
    val options = block?.let { PdfPrintOptions().apply(it) }
    return convertWeb2Pdf(Web2PdfRequest(data, options))
}