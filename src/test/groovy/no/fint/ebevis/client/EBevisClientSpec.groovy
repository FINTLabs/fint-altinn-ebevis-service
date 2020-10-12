package no.fint.ebevis.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.fint.ebevis.configuration.AltinnConfiguration
import no.fint.ebevis.model.ebevis.Accreditation
import no.fint.ebevis.model.ebevis.Error
import no.fint.ebevis.model.ebevis.Evidence
import no.fint.ebevis.util.MediaTypeSerializer
import no.fint.ebevis.util.ObjectFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.ZonedDateTime

class EBevisClientSpec extends Specification {
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModules(new JavaTimeModule(), new SimpleModule().addSerializer(MediaType.class, new MediaTypeSerializer()))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    MockWebServer mockWebServer = new MockWebServer()

    AltinnConfiguration altinnConfiguration = new AltinnConfiguration(
            baseUrl: 'http://localhost:' + mockWebServer.getPort(),
            ocpApimSubscriptionKey: 'key'
    )

    EBevisClient eBevisClient = new EBevisClient(WebClient.builder(), altinnConfiguration)

    void cleanup() {
        mockWebServer.shutdown()
    }

    def "create accreditation returns mono"() {
        given:
        def file = getClass().getClassLoader().getResource('accreditation.json')
        def accreditation = objectMapper.readValue(file, Accreditation.class)

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(accreditation))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .addHeader(HttpHeaders.LOCATION, 'location')
                .setResponseCode(HttpStatus.CREATED.value()))

        when:
        def setup = eBevisClient.createAccreditation(ObjectFactory.newAuthorization())

        then:
        StepVerifier.create(setup)
                .expectNextMatches({ responseEntity ->
                    responseEntity.statusCode == HttpStatus.CREATED &&
                            responseEntity.headers.getLocation() == URI.create('location') &&
                            responseEntity.body == accreditation
                })
                .verifyComplete()
    }

    def "delete accreditation returns mono"() {
        given:
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value()))

        when:
        def setup = eBevisClient.deleteAccreditation(_ as String)

        then:
        StepVerifier.create(setup)
                .expectNextMatches({ responseEntity ->
                    responseEntity.statusCode == HttpStatus.NO_CONTENT
                })
                .verifyComplete()
    }

    def "get accreditations returns flux"() {
        given:
        def file = getClass().getClassLoader().getResource('accreditation.json')
        def accreditation = objectMapper.readValue(file, Accreditation.class)

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([accreditation, accreditation]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = eBevisClient.getAccreditations(_ as String, ZonedDateTime.now(), _ as Boolean)

        then:
        StepVerifier.create(setup)
                .expectNext(accreditation, accreditation)
                .verifyComplete()
    }

    def "get evidence returns mono"() {
        given:
        def file = getClass().getClassLoader().getResource('evidence.json')
        def evidence = objectMapper.readValue(file, Evidence.class)

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(evidence))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = eBevisClient.getEvidence(_ as String, _ as String)

        then:
        StepVerifier.create(setup)
                .expectNext(evidence)
                .verifyComplete()
    }

    def "get evidence statuses returns flux"() {
        given:
        def file = getClass().getClassLoader().getResource('evidence.json')
        def evidenceStatus = objectMapper.readValue(file, Evidence.class).evidenceStatus

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([evidenceStatus, evidenceStatus]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = eBevisClient.getEvidenceStatuses(_ as String)

        then:
        StepVerifier.create(setup)
                .expectNext(evidenceStatus, evidenceStatus)
                .verifyComplete()
    }

    def "get error codes returns flux"() {
        given:
        def errorCode = new Error(code: 1, description: 'description')

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([errorCode, errorCode]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = eBevisClient.getErrorCodes()

        then:
        StepVerifier.create(setup)
                .expectNext(errorCode, errorCode)
                .verifyComplete()
    }

    def "get evidence codes returns flux"() {
        given:
        def file = getClass().getClassLoader().getResource('accreditation.json')
        def evidenceCode = objectMapper.readValue(file, Accreditation.class).evidenceCodes.first()

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([evidenceCode, evidenceCode]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = eBevisClient.getEvidenceCodes()

        then:
        StepVerifier.create(setup)
                .expectNext(evidenceCode, evidenceCode)
                .verifyComplete()
    }

    def "get status codes returns flux"() {
        given:
        def file = getClass().getClassLoader().getResource('evidence.json')
        def statusCode = objectMapper.readValue(file, Evidence.class).evidenceStatus.status

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([statusCode, statusCode]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = eBevisClient.getStatusCodes()

        then:
        StepVerifier.create(setup)
                .expectNext(statusCode, statusCode)
                .verifyComplete()
    }


}