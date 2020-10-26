package no.fint.ebevis.service

import no.fint.ebevis.client.DataAltinnClient
import no.fint.ebevis.model.AltinnApplication
import no.fint.ebevis.model.ConsentStatus
import no.fint.ebevis.model.AltinnApplicationStatus
import no.fint.ebevis.model.ebevis.Accreditation
import no.fint.ebevis.model.ebevis.Authorization
import no.fint.ebevis.model.ebevis.Evidence
import no.fint.ebevis.model.ebevis.EvidenceStatus
import no.fint.ebevis.model.ebevis.EvidenceStatusCode
import no.fint.ebevis.repository.AltinnApplicationRepository
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.OffsetDateTime

class ConsentServiceSpec extends Specification {
    DataAltinnClient client = Mock()
    AltinnApplicationRepository repository = Mock()

    ConsentService service = new ConsentService(client, repository)

    def "consentNew changes status when accreditation is accepted"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.NEW, requestor: 123, subject: 456, archiveReference: 'reference')
        def accreditation = new Accreditation(id: _ as String, evidenceCodes: [])

        when:
        service.checkForNewApplications()

        then:
        1 * repository.findAllByStatus(AltinnApplicationStatus.NEW) >> [application]
        1 * client.createAccreditation(_ as Authorization) >> Mono.just(ResponseEntity.created(URI.create('location')).body(accreditation))
        1 * repository.save(new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_REQUESTED, requestor: 123, subject: 456,
                archiveReference: 'reference', accreditationId: _ as String))
    }

    def "consentStatus changes consent status when consent is accepted"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_REQUESTED, accreditationId: _ as String,
                consents: [(_ as String): new AltinnApplication.Consent(evidenceCodeName: _ as String, status: ConsentStatus.CONSENT_REQUESTED)])
        def accepted = new EvidenceStatus(evidenceCodeName: _ as String, status: new EvidenceStatusCode(code: 1))

        when:
        service.checkForNewConsentStatuses()

        then:
        1 * client.getAccreditations(_ as OffsetDateTime) >> Mono.just([new Accreditation(id: _ as String)])
        1 * repository.findAllByAccreditationIdIn([_ as String]) >> [application]
        1 * client.getEvidenceStatuses(_ as String) >> Mono.just([accepted])
        1 * repository.save(new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_ACCEPTED, accreditationId: _ as String,
                consents: [(_ as String): new AltinnApplication.Consent(evidenceCodeName: _ as String, status: ConsentStatus.CONSENT_ACCEPTED)]))
    }
}
