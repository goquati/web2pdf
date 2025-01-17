package io.github.goquati.web2pdf.dsl

import io.github.goquati.web2pdf.apis.Web2PdfApi
import io.github.goquati.web2pdf.models.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
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

class PdfGenerationFailed(msg: String) : Exception(msg)

private suspend inline fun HttpResponse.toPdfChannel(): ByteReadChannel {
    if (!status.isSuccess())
        throw PdfGenerationFailed("pdf generation failed: ${runCatching { bodyAsText() }.getOrElse { "unknown error" }}")
    return bodyAsChannel()
}

suspend fun Web2PdfApi.convertHtml2Pdf(
    @Language("html") html: String,
    block: (PdfPrintOptions.() -> Unit)? = null,
): ByteReadChannel {
    val options = block?.let { PdfPrintOptions().apply(it) }
    return convertHtml2Pdf(Html2PdfRequest(html, options)).toPdfChannel()
}

suspend fun Web2PdfApi.convertMarkdown2Pdf(
    @Language("markdown") markdown: String,
    block: (PdfPrintOptions.() -> Unit)? = null,
): ByteReadChannel {
    val options = block?.let { PdfPrintOptions().apply(it) }
    return convertMarkdown2Pdf(Markdown2PdfRequest(markdown, options)).toPdfChannel()
}

suspend fun Web2PdfApi.convertWeb2Pdf(
    data: Web2Pdf,
    block: (PdfPrintOptions.() -> Unit)? = null,
): ByteReadChannel {
    val options = block?.let { PdfPrintOptions().apply(it) }
    return convertWeb2Pdf(Web2PdfRequest(data, options)).toPdfChannel()
}

suspend fun Web2PdfApi.createBaseDin5008(data: TemplateInvoiceBaseDin5008): ByteReadChannel =
     getInvoiceBaseDin5008(data).toPdfChannel()