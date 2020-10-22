package no.fint.ebevis.controller

import no.fint.ebevis.client.DataAltinnClient
import no.fint.ebevis.model.ebevis.Evidence
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import spock.lang.Specification

@WebFluxTest(EvidenceController.class)
class EvidenceControllerSpec extends Specification {
    @Autowired
    WebTestClient webTestClient

    @SpringBean
    DataAltinnClient client = Mock()

    def "Get application returns evidence"() {
        when:
        client.getEvidence(_ as String, _ as String) >> Mono.just(new Evidence())

        then:
        def first = webTestClient.get()
                .uri("/evidence/123?evidenceCodeName=test")
                .exchange()
                .expectStatus().isOk()
                .returnResult(Evidence.class)
                .getResponseBody()
                .blockFirst()

        first == new Evidence()
    }
}
