package no.fint.ebevis.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.fint.ebevis.configuration.AltinnProperties
import no.fint.ebevis.model.ebevis.Accreditation
import no.fint.ebevis.model.ebevis.ErrorCode
import no.fint.ebevis.model.ebevis.Evidence
import no.fint.ebevis.util.MediaTypeSerializer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import spock.lang.Specification

class DataAltinnMetadataClientSpec extends Specification {
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModules(new JavaTimeModule(), new SimpleModule().addSerializer(MediaType.class, new MediaTypeSerializer()))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    MockWebServer mockWebServer = new MockWebServer()

    AltinnProperties altinnProperties = Stub() {
        getBaseUrl() >> 'http://localhost:' + mockWebServer.getPort()
        getOcpApimSubscriptionKey() >> 'key'
    }

    DataAltinnMetadataClient dataAltinnMetadataClient = new DataAltinnMetadataClient(WebClient.builder(), altinnProperties)

    void cleanup() {
        mockWebServer.shutdown()
    }

    def "get error codes returns mono"() {
        given:
        def errorCode = new ErrorCode(code: 1, description: 'description')

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([errorCode, errorCode]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = dataAltinnMetadataClient.getErrorCodes()

        then:
        StepVerifier.create(setup)
                .expectNext([errorCode, errorCode])
                .verifyComplete()
    }

    def "get status codes returns mono"() {
        given:
        def file = getClass().getClassLoader().getResource('evidence.json')
        def statusCode = objectMapper.readValue(file, Evidence.class).evidenceStatus.status

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([statusCode, statusCode]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = dataAltinnMetadataClient.getStatusCodes()

        then:
        StepVerifier.create(setup)
                .expectNext([statusCode, statusCode])
                .verifyComplete()
    }

    def "get evidence codes returns mono"() {
        given:
        def file = getClass().getClassLoader().getResource('accreditation.json')
        def evidenceCode = objectMapper.readValue(file, Accreditation.class).evidenceCodes.first()

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([evidenceCode, evidenceCode]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = dataAltinnMetadataClient.getEvidenceCodes()

        then:
        StepVerifier.create(setup)
                .expectNext([evidenceCode, evidenceCode])
                .verifyComplete()
    }

    def "get service contexts returns mono"() {
        given:
        mockWebServer.enqueue(new MockResponse()
                .setBody('["Drosjeloyve", "eBevis"]')
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = dataAltinnMetadataClient.getServiceContexts()

        then:
        StepVerifier.create(setup)
                .expectNext(["Drosjeloyve", "eBevis"])
                .verifyComplete()
    }

    def "get evidence codes within service context returns mono"() {
        given:
        def file = getClass().getClassLoader().getResource('accreditation.json')
        def evidenceCode = objectMapper.readValue(file, Accreditation.class).evidenceCodes.first()

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString([evidenceCode, evidenceCode]))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value()))

        when:
        def setup = dataAltinnMetadataClient.getEvidenceCodesWithinServiceContext(_ as String)

        then:
        StepVerifier.create(setup)
                .expectNext([evidenceCode, evidenceCode])
                .verifyComplete()
    }
}
