package no.fint.ebevis.service

import no.fint.ebevis.client.DataAltinnClient
import no.fint.ebevis.model.AltinnApplication
import no.fint.ebevis.model.AltinnApplicationConsentStatus
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

class ConsentServiceSpec extends Specification {
    DataAltinnClient client = Mock()
    AltinnApplicationRepository repository = Mock()

    ConsentService service = new ConsentService(client, repository)

    def "consentNew changes status when accreditation is accepted"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.NEW, requestor: 123, subject: 456, archiveReference: 'reference')
        def accreditation = new Accreditation(id: _ as String)

        when:
        service.consentNew()

        then:
        1 * repository.findByStatus(AltinnApplicationStatus.NEW) >> [application]
        1 * client.createAccreditation(_ as Authorization) >> Mono.just(ResponseEntity.created(URI.create('location')).body(accreditation))
        1 * repository.save(new AltinnApplication(status: AltinnApplicationStatus.CONSENT_REQUESTED, accreditationId: _ as String,
                requestor: 123, subject: 456, archiveReference: 'reference'))
    }

    def "consentStatus changes consent status when consent is accepted"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENT_REQUESTED, accreditationId: _ as String)
        def accepted = new EvidenceStatus(evidenceCodeName: _ as String, status: new EvidenceStatusCode(code: 1))
        def requested = new EvidenceStatus(evidenceCodeName: _ as String, status: new EvidenceStatusCode(code: 2))

        when:
        service.consentStatus()

        then:
        1 * repository.findByStatus(AltinnApplicationStatus.CONSENT_REQUESTED) >> [application]
        1 * client.getEvidenceStatuses(_ as String) >> Mono.just([accepted, requested])
        1 * repository.save(new AltinnApplication(status: AltinnApplicationStatus.CONSENT_REQUESTED, accreditationId: _ as String,
                consents: [(_ as String): AltinnApplicationConsentStatus.CONSENT_ACCEPTED, (_ as String): AltinnApplicationConsentStatus.CONSENT_REQUESTED]))
    }

    def "evidence changes consent status when consent is accepted"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENT_REQUESTED, accreditationId: _ as String,
                consents: [(_ as String): AltinnApplicationConsentStatus.CONSENT_ACCEPTED])

        when:
        service.evidence()

        then:
        1 * repository.findByStatus(AltinnApplicationStatus.CONSENT_REQUESTED) >> [application]
        1 * client.getEvidence(_ as String, _ as String) >> Mono.just(new Evidence())
        1 * repository.save(new AltinnApplication(status: AltinnApplicationStatus.EVIDENCE_FETCHED, accreditationId: _ as String,
                consents: [(_ as String): AltinnApplicationConsentStatus.CONSENT_ACCEPTED], evidence: [new Evidence()]))
    }
}
