package no.novari.ebevis.controller

import no.novari.fint.altinn.model.AltinnApplication
import no.novari.fint.altinn.model.AltinnApplicationStatus
import no.novari.ebevis.repository.AltinnApplicationRepository
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest(AltinnApplicationController.class)
class AltinnApplicationControllerSpec extends Specification {

    @Autowired
    WebTestClient webTestClient

    @SpringBean
    AltinnApplicationRepository repository = Mock()

    def "updateStatus sets status and returns updated application"() {
        given:
        def application = new AltinnApplication(archiveReference: '69483923-6fd7cd6c-8096-4272-9777-e207ee4aef0c', status: AltinnApplicationStatus.PURGED)

        when:
        repository.findById('69483923-6fd7cd6c-8096-4272-9777-e207ee4aef0c') >> Optional.of(application)
        repository.save(_ as AltinnApplication) >> { AltinnApplication app -> app }

        then:
        webTestClient.patch()
                .uri("/applications/69483923-6fd7cd6c-8096-4272-9777-e207ee4aef0c/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue('"CONSENTS_ACCEPTED"')
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath('$.status').isEqualTo('CONSENTS_ACCEPTED')
    }

    def "updateStatus returns 404 when application is not found"() {
        when:
        repository.findById('unknown') >> Optional.empty()

        then:
        webTestClient.patch()
                .uri("/applications/unknown/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue('"CONSENTS_ACCEPTED"')
                .exchange()
                .expectStatus().isNotFound()
    }

    def "updateStatus rejects a value that is not a valid AltinnApplicationStatus"() {
        when:
        webTestClient.patch()
                .uri("/applications/69483923-6fd7cd6c-8096-4272-9777-e207ee4aef0c/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue('"NOT_A_REAL_STATUS"')
                .exchange()
                .expectStatus().isBadRequest()

        then:
        0 * repository.findById(_)
        0 * repository.save(_)
    }
}
