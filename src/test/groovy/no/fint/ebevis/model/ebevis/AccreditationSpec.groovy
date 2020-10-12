package no.fint.ebevis.model.ebevis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import spock.lang.Specification

import java.time.ZonedDateTime

class AccreditationSpec extends Specification {
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())

    def "accreditation deserializes json object"() {
        given:
        def file = getClass().getClassLoader().getResource('accreditation.json')
        def zonedDateTime = ZonedDateTime.parse('2020-01-01T00:00:30Z[UTC]')

        when:
        def accreditation = objectMapper.readValue(file, Accreditation.class)

        then:
        accreditation.accreditationId == 'accreditationId'
        accreditation.requestor == 'requestor'
        accreditation.subject == 'subject'
        accreditation.aggregateStatus.code == 1
        accreditation.aggregateStatus.description == 'description'
        accreditation.aggregateStatus.retryAt == zonedDateTime
        accreditation.evidenceCodes.first().evidenceCodeName == 'evidenceCodeName'
        accreditation.evidenceCodes.first().description == 'description'
        accreditation.evidenceCodes.first().accessMethod.value == 'open'
        accreditation.evidenceCodes.first().maxValidDays == 2
        accreditation.evidenceCodes.first().isAsynchronous
        accreditation.evidenceCodes.first().values.first().evidenceValueName == 'evidenceValueName'
        accreditation.evidenceCodes.first().values.first().valueType.value == 'string'
        accreditation.evidenceCodes.first().values.first().mimeType.toString() == 'application/json'
        accreditation.evidenceCodes.first().values.first().source == 'source'
        accreditation.evidenceCodes.first().values.first().value == 'value'
        accreditation.evidenceCodes.first().values.first().timestamp == zonedDateTime
        accreditation.evidenceCodes.first().parameters.first().evidenceParamName == 'evidenceParamName'
        accreditation.evidenceCodes.first().parameters.first().paramType.value == 'string'
        accreditation.evidenceCodes.first().parameters.first().required
        accreditation.evidenceCodes.first().parameters.first().value == 'value'
        accreditation.issued == zonedDateTime
        accreditation.lastChanged == zonedDateTime
        accreditation.validTo == zonedDateTime
        accreditation.tedReference == 'tedReference'
        accreditation.doffinReference == 'doffinReference'
        accreditation.externalReference == 'externalReference'
    }
}
