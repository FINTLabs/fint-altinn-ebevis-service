package no.novari.ebevis.util

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import no.novari.fint.altinn.model.AltinnApplication
import no.novari.fint.altinn.model.ebevis.Evidence
import no.novari.fint.altinn.model.ebevis.EvidenceStatus
import no.novari.fint.altinn.model.ebevis.EvidenceValue
import no.novari.fint.altinn.model.ebevis.vocab.ValueType
import spock.lang.Specification

import java.io.ByteArrayInputStream
import java.time.OffsetDateTime

class CertificateConverterSpec extends Specification {

    def "convertBankruptCertificate maps KonkursDrosje values"() {
        given:
        def converter = new CertificateConverter()
        converter.setFontFile("classpath:times.ttf")

        def application = new AltinnApplication()
        application.setSubjectName("QUDSIYA ARCTIC TAXI & TOURS")
        application.setSubject("937967373")

        def evidenceStatus = new EvidenceStatus()
        evidenceStatus.setEvidenceCodeName("KonkursDrosje")

        def sourceValue = new EvidenceValue()
        sourceValue.setEvidenceValueName("Organisasjonsnavn")
        sourceValue.setValueType(ValueType.STRING)
        sourceValue.setSource("Brønnøysundregistrene")
        sourceValue.setTimestamp(OffsetDateTime.parse("2026-07-27T06:57:02.686931400Z"))
        sourceValue.setValue("QUDSIYA ARCTIC TAXI & TOURS")

        def konkursValue = new EvidenceValue()
        konkursValue.setEvidenceValueName("Konkurs")
        konkursValue.setValueType(ValueType.BOOLEAN)
        konkursValue.setValue(false)

        def underAvviklingValue = new EvidenceValue()
        underAvviklingValue.setEvidenceValueName("UnderAvvikling")
        underAvviklingValue.setValueType(ValueType.BOOLEAN)
        underAvviklingValue.setValue(false)

        def tvangsavviklingValue = new EvidenceValue()
        tvangsavviklingValue.setEvidenceValueName("UnderTvangsavviklingEllerTvangsopplosning")
        tvangsavviklingValue.setValueType(ValueType.BOOLEAN)
        tvangsavviklingValue.setValue(false)

        def evidence = new Evidence()
        evidence.setEvidenceStatus(evidenceStatus)
        evidence.setEvidenceValues([sourceValue, konkursValue, underAvviklingValue, tvangsavviklingValue])

        when:
        byte[] pdf = converter.convertCertificate(evidence, application, "KonkursDrosje")
        def text = extractText(pdf)

        then:
        pdf != null
        text.contains("Bekreftelse fra Konkursregisteret")
        text.contains("Kilde: Brønnøysundregistrene 2026-07-27")
        text.contains("Konkurs: nei")
        text.contains("Under avvikling: nei")
        text.contains("Under tvangsavvikling eller tvangsoppløsning: nei")
    }

    def "convertTaxCertificate maps legacy flat RestanserV2 values"() {
        given:
        def converter = new CertificateConverter()
        converter.setFontFile("classpath:times.ttf")

        def application = new AltinnApplication()
        application.setSubjectName("QUDSIYA ARCTIC TAXI & TOURS")
        application.setSubject("937967373")

        def evidenceStatus = new EvidenceStatus()
        evidenceStatus.setEvidenceCodeName("RestanserV2")

        def levert = new EvidenceValue()
        levert.setEvidenceValueName("levert")
        levert.setValueType(ValueType.STRING)
        levert.setSource("Skatteetaten")
        levert.setTimestamp(OffsetDateTime.parse("2026-07-27T06:57:02.686931400Z"))
        levert.setValue("2026-07-27T08:57:02.472187")

        def arbeidsgiveravgift = new EvidenceValue()
        arbeidsgiveravgift.setEvidenceValueName("arbeidsgiveravgiftForfaltOgUbetalt")
        arbeidsgiveravgift.setValueType(ValueType.NUMBER)
        arbeidsgiveravgift.setValue(0.0)

        def forskuddstrekk = new EvidenceValue()
        forskuddstrekk.setEvidenceValueName("forskuddstrekkForfaltOgUbetalt")
        forskuddstrekk.setValueType(ValueType.NUMBER)
        forskuddstrekk.setValue(12.5)

        def forskuddsskatt = new EvidenceValue()
        forskuddsskatt.setEvidenceValueName("forskuddsskattForfaltOgUbetalt")
        forskuddsskatt.setValueType(ValueType.NUMBER)
        forskuddsskatt.setValue(25.0)

        def restskatt = new EvidenceValue()
        restskatt.setEvidenceValueName("restskattForfaltOgUbetalt")
        restskatt.setValueType(ValueType.NUMBER)
        restskatt.setValue(37.5)

        def gebyr = new EvidenceValue()
        gebyr.setEvidenceValueName("gebyrForfaltOgUbetalt")
        gebyr.setValueType(ValueType.NUMBER)
        gebyr.setValue(50.0)

        def merverdiavgift = new EvidenceValue()
        merverdiavgift.setEvidenceValueName("merverdiavgiftForfaltOgUbetalt")
        merverdiavgift.setValueType(ValueType.NUMBER)
        merverdiavgift.setValue(62.5)

        def evidence = new Evidence()
        evidence.setEvidenceStatus(evidenceStatus)
        evidence.setEvidenceValues([
                levert,
                arbeidsgiveravgift,
                forskuddstrekk,
                forskuddsskatt,
                restskatt,
                gebyr,
                merverdiavgift
        ])

        when:
        byte[] pdf = converter.convertCertificate(evidence, application, "RestanserV2")
        def text = extractText(pdf)

        then:
        pdf != null
        text.contains("Kilde: Skatteetaten 2026-07-27")
        text.contains("Arbeidsgiveravgift forfalt og ubetalt: 0.0")
        text.contains("Forskuddstrekk forfalt og ubetalt: 12.5")
        text.contains("Forskuddsskatt forfalt og ubetalt: 25.0")
        text.contains("Restskatt forfalt og ubetalt: 37.5")
        text.contains("Gebyr forfalt og ubetalt: 50.0")
        text.contains("Merverdiavgift forfalt og ubetalt: 62.5")
    }

    def "convertTaxCertificate maps current RestanserV2 default JSON payload"() {
        given:
        def converter = new CertificateConverter()
        converter.setFontFile("classpath:times.ttf")

        def application = new AltinnApplication()
        application.setSubjectName("QUDSIYA ARCTIC TAXI & TOURS")
        application.setSubject("937967373")

        def evidenceStatus = new EvidenceStatus()
        evidenceStatus.setEvidenceCodeName("RestanserV2")

        def restanser = [
                arbeidsgiveravgift: [forfaltOgUbetalt: 0.0],
                forskuddstrekk    : [forfaltOgUbetalt: 0.0],
                forskuddsskatt    : [forfaltOgUbetalt: 0.0],
                restskatt         : [forfaltOgUbetalt: 0.0],
                gebyr             : [forfaltOgUbetalt: 0.0],
                merverdiavgift    : [forfaltOgUbetalt: 0.0]
        ]

        def defaultValue = new EvidenceValue()
        defaultValue.setEvidenceValueName("default")
        defaultValue.setValueType(ValueType.JSON_SCHEMA)
        defaultValue.setSource("Skatteetaten")
        defaultValue.setTimestamp(OffsetDateTime.parse("2026-07-27T06:57:02.686931400Z"))
        defaultValue.setValue([
                levert                : "2026-07-27T08:57:02.472187",
                forespurteOrganisasjon: "937967373",
                restanser             : restanser
        ])

        def evidence = new Evidence()
        evidence.setEvidenceStatus(evidenceStatus)
        evidence.setEvidenceValues([defaultValue])

        when:
        byte[] pdf = converter.convertCertificate(evidence, application, "RestanserV2")
        def text = extractText(pdf)

        then:
        pdf != null
        text.contains("Kilde: Skatteetaten 2026-07-27")
        text.contains("Arbeidsgiveravgift forfalt og ubetalt: 0")
        text.contains("Forskuddstrekk forfalt og ubetalt: 0")
        text.contains("Forskuddsskatt forfalt og ubetalt: 0")
        text.contains("Restskatt forfalt og ubetalt: 0")
        text.contains("Gebyr forfalt og ubetalt: 0")
        text.contains("Merverdiavgift forfalt og ubetalt: 0")
    }

    private static String extractText(byte[] pdfBytes) {
        def builder = new StringBuilder()
        def pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))

        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
            builder.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i)))
        }

        pdfDocument.close()
        return builder.toString()
    }
}
