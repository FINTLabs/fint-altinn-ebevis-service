package no.fint.ebevis.model.ebevis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZonedDateTime

class EvidenceSpec extends Specification {
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())

    def "evidence deserializes json object"() {
        given:
        def file = getClass().getClassLoader().getResource('evidence.json')
        def offsetDateTime = OffsetDateTime.parse('2020-01-01T00:00:30Z')

        when:
        def evidence = objectMapper.readValue(file, Evidence.class)

        then:
        evidence.name == 'name'
        evidence.timestamp ==  offsetDateTime
        evidence.evidenceStatus.evidenceCodeName == 'evidenceCodeName'
        evidence.evidenceStatus.status.description == 'description'
        evidence.evidenceStatus.status.code == 1
        evidence.evidenceStatus.status.retryAt == offsetDateTime
        evidence.evidenceStatus.validFrom == offsetDateTime
        evidence.evidenceStatus.validTo == offsetDateTime
        evidence.evidenceStatus.didSupplyLegalBasis
        evidence.evidenceValues.first().evidenceValueName == 'evidenceValueName'
        evidence.evidenceValues.first().valueType.value == 'string'
        evidence.evidenceValues.first().mimeType.toString() == 'application/json'
        evidence.evidenceValues.first().source == 'source'
        evidence.evidenceValues.first().value == 'value'
        evidence.evidenceValues.first().timestamp == offsetDateTime
    }
}
