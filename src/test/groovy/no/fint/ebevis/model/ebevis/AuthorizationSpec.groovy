package no.fint.ebevis.model.ebevis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.fint.ebevis.util.ObjectFactory
import spock.lang.Specification

class AuthorizationSpec extends Specification {
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    def "authorization serializes java object"() {
        given:
        def file = getClass().getClassLoader().getResource('authorization.json')
        def string = objectMapper.writeValueAsString(ObjectFactory.newAuthorization())

        expect:
        objectMapper.readTree(file) == objectMapper.readTree(string)
    }
}
