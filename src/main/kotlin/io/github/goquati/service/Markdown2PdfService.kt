package io.github.goquati.service

import io.github.goquati.oas.oas_model.PdfPrintOptionsDto
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.springframework.stereotype.Service

@Service
class Markdown2PdfService(
    private val html2PdfService: Html2PdfService,
) {
    suspend fun generatePdf(
        data: String,
        options: PdfPrintOptionsDto?,
    ) = html2PdfService.generatePdf(
        data = data.toHtml(),
        options = options,
    )

    private final val flavour = CommonMarkFlavourDescriptor()
    private final val markdownParser = MarkdownParser(flavour)

    private fun String.toHtml() = HtmlGenerator(
        markdownText = this,
        root = markdownParser.buildMarkdownTreeFromString(this),
        flavour = flavour,
    ).generateHtml()
}
