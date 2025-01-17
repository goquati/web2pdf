package io.github.goquati.service

import io.github.goquati.Web2PdfException
import io.github.goquati.oas.oas_model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


@Service
class Template2PdfService(
    private val web2PdfService: Web2PdfService,
    private val environmentService: EnvironmentService,
    private val mustacheService: MustacheService,
) {
    private val htmlCache = MutableStateFlow<MutableMap<UUID, Template>>(mutableMapOf())

    suspend fun generatePdf(
        data: TemplateInvoiceBaseDin5008Dto,
    ): ByteArray {
        val id = UUID.randomUUID()!!
        htmlCache.update { it[id] = Template.InvoiceBaseDin5008(data); it }
        try {
            return web2PdfService.generatePdf(
                url = environmentService.getLocalServerUrl("template2pdf-data/$id"),
                options = printOptionsA4,
            )
        } finally {
            htmlCache.update { it.remove(id); it }
        }
    }

    suspend fun getHtml(id: UUID): String {
        val template = when (id) {
            UUID.fromString("7cc7e89b-26c5-4da4-b355-00d8add0b5e3") -> Template.InvoiceBaseDin5008.dummy
            else -> htmlCache.value[id]
        } ?: throw Web2PdfException("html data not found", status = HttpStatus.NOT_FOUND)
        return mustacheService.process(path = template.templatePath, data = template.data)
    }

    private sealed interface Template {
        val raw: Any
        val templatePath: String
        val additionalFields: Map<String, Any?>

        val data: Any
            get() {
                @Suppress("UNCHECKED_CAST")
                val result = (raw::class as KClass<Any>).memberProperties.associate {
                    it.name to it.get(raw)
                }.toMutableMap()
                result.putAll(additionalFields)
                return result.toMap()
            }


        data class InvoiceBaseDin5008(
           override val raw: TemplateInvoiceBaseDin5008Dto
        ) : Template {
            override val templatePath = "templates/invoice-base-din-5008.html"
            override val additionalFields: Map<String, Any?>
                get() = mapOf(
                    "isTypeA" to (raw.type == TemplateInvoiceBaseDin5008Dto.Type.A),
                )

            companion object {
                val dummy
                    get() = InvoiceBaseDin5008(
                        TemplateInvoiceBaseDin5008Dto(
                            type = TemplateInvoiceBaseDin5008Dto.Type.A,
                            sendBackAddress = "Firma GmbH | Musterstraße 1 | 12345 Musterstadt",
                            receiverInfo = listOf(),
                            receiver = listOf(
                                "Kundenname",
                                "Firmenadresse 123",
                                "98765 Kundenstadt",
                            ),
                            sender = listOf(
                                "Firma GmbH",
                                "Musterstraße 1",
                                "12345 Musterstadt",
                                "Umsatzsteuer-ID: DE123456789",
                                "Handelsregister: HRB 12345",
                            ),
                            subject = "Rechnung",
                            details = listOf(
                                TemplateInvoiceBaseDin5008DetailsInnerDto(
                                    title = "Rechnungsnummer",
                                    value = "2024-001"
                                ),
                                TemplateInvoiceBaseDin5008DetailsInnerDto(
                                    title = "Rechnungsdatum",
                                    value = "10. September 2024"
                                ),
                                TemplateInvoiceBaseDin5008DetailsInnerDto(
                                    title = "Fälligkeitsdatum",
                                    value = "24. September 2024"
                                ),
                                TemplateInvoiceBaseDin5008DetailsInnerDto(
                                    title = "Leistungsdatum",
                                    value = "10. September 2024"
                                ),
                            ),
                            itemHeader = TemplateInvoiceBaseDin5008RowDto(
                                description = "Beschreibung",
                                quantity = "Menge",
                                unitPrice = "Einzelpreis",
                                total = "Gesamt"
                            ),
                            items = listOf(
                                TemplateInvoiceBaseDin5008RowDto(
                                    description = "Produkt A",
                                    quantity = "1",
                                    unitPrice = "100,00 €",
                                    total = "100,00 €"
                                ),
                                TemplateInvoiceBaseDin5008RowDto(
                                    description = "Produkt B",
                                    quantity = "2",
                                    unitPrice = "50,00 €",
                                    total = "100,00 €"
                                ),
                            ),
                            summary = listOf(
                                TemplateInvoiceBaseDin5008SummaryInnerDto(
                                    title = "Zwischensumme",
                                    value = "200,00 €",
                                ),
                                TemplateInvoiceBaseDin5008SummaryInnerDto(
                                    title = "Umsatzsteuer (19%)",
                                    value = "38,00 €",
                                ),
                                TemplateInvoiceBaseDin5008SummaryInnerDto(
                                    title = "Gesamtbetrag",
                                    value = "238,00 €",
                                    bold = true
                                ),
                            ),
                            footer = listOf(
                                "Vielen Dank für Ihr Vertrauen in unser Unternehmen.",
                                "Zahlbar innerhalb von 14 Tagen auf folgendes Konto:",
                                "Bankverbindung: IBAN: DE12345678901234567890 | BIC: ABCDDEFFXXX",
                            ),
                        )
                    )
            }
        }
    }

    companion object {
        private val printOptionsA4 = PdfPrintOptionsDto(
            marginTop = 0.0,
            marginBottom = 0.0,
            marginLeft = 0.0,
            marginRight = 0.0,
            paperWidth = 8.3,
            paperHeight = 11.7,
            printBackground = true,
        )
    }
}