package no.fint.ebevis.service

import no.fint.ebevis.client.DataAltinnClient
import no.fint.ebevis.model.AltinnApplication
import no.fint.ebevis.model.ConsentStatus
import no.fint.ebevis.model.AltinnApplicationStatus
import no.fint.ebevis.model.ebevis.Accreditation
import no.fint.ebevis.model.ebevis.Authorization
import no.fint.ebevis.model.ebevis.EvidenceStatus
import no.fint.ebevis.model.ebevis.EvidenceStatusCode
import no.fint.ebevis.repository.AltinnApplicationRepository
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Specification

import java.time.OffsetDateTime

class ConsentServiceSpec extends Specification {
    DataAltinnClient client = Mock()
    AltinnApplicationRepository repository = Mock()

    ConsentService service = new ConsentService(client, repository)

    def "consentNew changes status when accreditation is accepted"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.NEW, requestor: 123, subject: 456, archiveReference: 'reference')

        when:
        service.create()

        then:
        1 * repository.findAllByStatus(AltinnApplicationStatus.NEW) >> [application]
        StepVerifier.create(Flux.fromIterable([application]))
                .expectNext(application)
                .verifyComplete()
    }

    def "consentStatus changes consent status when consent is accepted"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_REQUESTED, accreditationId: _ as String,
                consents: [(_ as String): new AltinnApplication.Consent(evidenceCodeName: _ as String, status: ConsentStatus.CONSENT_REQUESTED)])

        when:
        service.update()

        then:
        1 * client.getAccreditations(_ as OffsetDateTime) >> Mono.just([new Accreditation(id: _ as String)])
        1 * repository.findAllByAccreditationIdIn([_ as String]) >> [application]
        StepVerifier.create(Flux.fromIterable([application]))
                .expectNext(application)
                .verifyComplete()
    }
}
