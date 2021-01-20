package no.fint.ebevis.service

import no.fint.altinn.model.AltinnApplication
import no.fint.altinn.model.AltinnApplicationStatus
import no.fint.altinn.model.ConsentStatus
import no.fint.altinn.model.ebevis.Accreditation
import no.fint.altinn.model.ebevis.ErrorCode
import no.fint.altinn.model.ebevis.EvidenceStatus
import no.fint.altinn.model.ebevis.EvidenceStatusCode
import no.fint.ebevis.client.DataAltinnClient
import no.fint.ebevis.configuration.MongoConfiguration
import no.fint.ebevis.exception.AltinnException
import no.fint.ebevis.repository.AltinnApplicationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.OffsetDateTime

@DataMongoTest
@Import(MongoConfiguration.class)
class EvidenceServiceSpec extends Specification {
    DataAltinnClient client = Mock()

    @Autowired
    AltinnApplicationRepository repository

    EvidenceService service

    void setup() {
        service = new EvidenceService(client, repository)
    }

    void cleanup() {
        repository.deleteAll()
    }

    def "updateEvidence updates application with new data and status"() {
        given:
        repository.save(new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                accreditationId: '1', accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z'), accreditationCount: 0,
                consents: [('KonkursDrosje')  : new AltinnApplication.Consent(evidenceCodeName: 'KonkursDrosje', status: ConsentStatus.CONSENT_REQUESTED),
                           ('RestanserDrosje'): new AltinnApplication.Consent(evidenceCodeName: 'RestanserDrosje', status: ConsentStatus.CONSENT_REQUESTED)]))

        1 * client.getEvidenceStatuses(_ as String) >> Mono.just([new EvidenceStatus(evidenceCodeName: 'KonkursDrosje', status: new EvidenceStatusCode(code: 1)),
                                                                  new EvidenceStatus(evidenceCodeName: 'RestanserDrosje', status: new EvidenceStatusCode(code: 1))])

        when:
        def evidence = service.updateEvidence()

        then:
        1 * client.getAccreditations(_ as OffsetDateTime) >> Mono.just([new Accreditation(id: '1')])

        StepVerifier.create(evidence)
                .expectNextMatches({ application ->
                    application.accreditationId == '1' &&
                            application.consents.get('KonkursDrosje').status == ConsentStatus.CONSENT_ACCEPTED &&
                            application.consents.get('RestanserDrosje').status == ConsentStatus.CONSENT_ACCEPTED &&
                            application.status == AltinnApplicationStatus.CONSENTS_ACCEPTED
                })
                .verifyComplete()

        def application = repository.findById('1')
        application.isPresent()
        application.get().accreditationId == '1'
        application.get().consents.get('KonkursDrosje').status == ConsentStatus.CONSENT_ACCEPTED
        application.get().consents.get('RestanserDrosje').status == ConsentStatus.CONSENT_ACCEPTED
        application.get().status == AltinnApplicationStatus.CONSENTS_ACCEPTED
    }

    def "updateEvidence continues on error"() {
        given:
        repository.saveAll([new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                accreditationId: '1', accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z'), accreditationCount: 0),
                            new AltinnApplication(requestor: '2', subject: '2', archiveReference: '2', status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                                    accreditationId: '2', accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z'), accreditationCount: 0)])

        2 * client.getEvidenceStatuses(_ as String) >>> [Mono.error(new AltinnException(HttpStatus.INTERNAL_SERVER_ERROR, new ErrorCode(code: 1003, description: 'description'))),
                                                         Mono.just([new EvidenceStatus(evidenceCodeName: 'KonkursDrosje', status: new EvidenceStatusCode(code: 1)),
                                                                    new EvidenceStatus(evidenceCodeName: 'RestanserDrosje', status: new EvidenceStatusCode(code: 1))])]

        when:
        def evidence = service.updateEvidence()

        then:
        1 * client.getAccreditations(_ as OffsetDateTime) >> Mono.just([new Accreditation(id: '1'), new Accreditation(id: '2')])

        StepVerifier.create(evidence)
                .expectNextCount(1)
                .verifyComplete()
    }
}
