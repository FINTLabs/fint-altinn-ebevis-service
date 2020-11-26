package no.fint.ebevis.service

import no.fint.ebevis.client.DataAltinnClient
import no.fint.ebevis.exception.AltinnException
import no.fint.ebevis.model.AltinnApplication
import no.fint.ebevis.model.ConsentStatus
import no.fint.ebevis.model.AltinnApplicationStatus
import no.fint.ebevis.model.ebevis.Accreditation
import no.fint.ebevis.model.ebevis.Authorization
import no.fint.ebevis.model.ebevis.ErrorCode
import no.fint.ebevis.model.ebevis.EvidenceCode
import no.fint.ebevis.model.ebevis.EvidenceStatus
import no.fint.ebevis.model.ebevis.EvidenceStatusCode
import no.fint.ebevis.model.ebevis.Notification
import no.fint.ebevis.repository.AltinnApplicationRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.OffsetDateTime

class ConsentServiceSpec extends Specification {
    DataAltinnClient client = Mock()

    AltinnApplicationRepository repository = Mock()

    ConsentService service = new ConsentService(client, repository)

    def "createAccreditations"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.NEW, requestor: 1, subject: 2, archiveReference: _ as String)

        when:
        service.createAccreditations()

        then:
        1 * repository.findAllByStatus(AltinnApplicationStatus.NEW) >> [application]
    }

    def "createAccreditation changes status when consent is requested"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.NEW, requestor: 1, subject: 2, archiveReference: _ as String)
        def accreditation = new Accreditation(id: 'id', issued: OffsetDateTime.parse('2000-01-01T00:00:00Z'), evidenceCodes: [new EvidenceCode(evidenceCodeName: _ as String)])

        when:
        service.createAccreditation(application)

        then:
        1 * client.createAccreditation(_ as Authorization) >> Mono.just(ResponseEntity.created(URI.create('uri')).body(accreditation))
        1 * repository.save(new AltinnApplication(
                requestor: 1,
                subject: 2,
                archiveReference: _ as String,
                status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                accreditationId: 'id',
                accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z'),
                accreditationCount: 0,
                consents: [(_ as String): new AltinnApplication.Consent(status: ConsentStatus.CONSENT_REQUESTED, evidenceCodeName: _ as String)]))
    }

    def "createAccreditation changes status on invalid subject"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.NEW, requestor: 1, subject: 2, archiveReference: _ as String)

        when:
        service.createAccreditation(application)

        then:
        1 * client.createAccreditation(_ as Authorization) >> Mono.error(new AltinnException(HttpStatus.INTERNAL_SERVER_ERROR, new ErrorCode(code: 1004)))
        1 * repository.save(new AltinnApplication(
                requestor: 1,
                subject: 2,
                archiveReference: _ as String,
                status: AltinnApplicationStatus.CONSENTS_INVALID_SUBJECT))
    }

    def "updateStatuses"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_REQUESTED, accreditationId: _ as String)

        when:
        service.updateStatuses()

        then:
        1 * client.getAccreditations(_ as OffsetDateTime) >> Mono.just([new Accreditation(id: _ as String)])
        1 * repository.findAllByStatusInAndAccreditationIdIn([AltinnApplicationStatus.CONSENTS_REQUESTED, AltinnApplicationStatus.CONSENTS_ACCEPTED], [_ as String]) >> [application]
    }

    def "updateStatus changes status when consent is accepted"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_ACCEPTED, accreditationId: 'id',
                consents: [('id1'): new AltinnApplication.Consent(evidenceCodeName: 'id1', status: ConsentStatus.CONSENT_ACCEPTED),
                           ('id2'): new AltinnApplication.Consent(evidenceCodeName: 'id2', status: ConsentStatus.CONSENT_ACCEPTED)])
        def evidenceStatus = new EvidenceStatus(evidenceCodeName: 'id1', status: new EvidenceStatusCode(code: 1))
        def evidenceStatus2 = new EvidenceStatus(evidenceCodeName: 'id2', status: new EvidenceStatusCode(code: 3))


        when:
        service.updateStatus(application)

        then:
        1 * client.getEvidenceStatuses(_ as String) >> Mono.just([evidenceStatus, evidenceStatus2])
        1 * repository.save(new AltinnApplication(
                status: AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED,
                accreditationId: 'id',
                consents: [('id1'): new AltinnApplication.Consent(evidenceCodeName: 'id1', status: ConsentStatus.CONSENT_ACCEPTED),
                           ('id2'): new AltinnApplication.Consent(evidenceCodeName: 'id2', status: ConsentStatus.CONSENT_REJECTED)]))
    }

    def "updateStatus does not change status if evidence is fetched before consent is rejected"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_ACCEPTED, accreditationId: 'id',
                consents: [('id1'): new AltinnApplication.Consent(evidenceCodeName: 'id1', status: ConsentStatus.CONSENT_ACCEPTED, documentId: 'id1'),
                           ('id2'): new AltinnApplication.Consent(evidenceCodeName: 'id2', status: ConsentStatus.CONSENT_ACCEPTED, documentId: 'id2')])
        def evidenceStatus = new EvidenceStatus(evidenceCodeName: 'id1', status: new EvidenceStatusCode(code: 1))
        def evidenceStatus2 = new EvidenceStatus(evidenceCodeName: 'id2', status: new EvidenceStatusCode(code: 3))

        when:
        service.updateStatus(application)

        then:
        1 * client.getEvidenceStatuses(_ as String) >> Mono.just([evidenceStatus, evidenceStatus2])
        1 * repository.save(new AltinnApplication(
                status: AltinnApplicationStatus.CONSENTS_ACCEPTED,
                accreditationId: 'id',
                consents: [('id1'): new AltinnApplication.Consent(evidenceCodeName: 'id1', status: ConsentStatus.CONSENT_ACCEPTED, documentId: 'id1'),
                           ('id2'): new AltinnApplication.Consent(evidenceCodeName: 'id2', status: ConsentStatus.CONSENT_REJECTED, documentId: 'id2')]))
    }

    def "sendReminders"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_REQUESTED, accreditationId: _ as String,
                accreditationDate: OffsetDateTime.now().minusDays(8), accreditationCount: 1)

        when:
        service.sendReminders()

        then:
        1 * repository.findAllByStatus(AltinnApplicationStatus.CONSENTS_REQUESTED) >> [application]
    }

    def "sendReminder sends reminder"() {
        given:
        def application = new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_REQUESTED, accreditationId: 'id')
        def notification = new Notification(success: true, recipientCount: 1, date: OffsetDateTime.parse('2000-01-01T00:00:00Z'))

        when:
        service.sendReminder(application)

        then:
        1 * client.createReminder(_ as String) >> Mono.just([notification])
        1 * repository.save(new AltinnApplication(
                status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                accreditationId: 'id',
                accreditationCount: 1,
                accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z')))
    }
}
