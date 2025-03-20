package no.novari.ebevis.service

import no.fint.altinn.model.AltinnApplication
import no.fint.altinn.model.AltinnApplicationStatus
import no.fint.altinn.model.ConsentStatus
import no.fint.altinn.model.ebevis.Accreditation
import no.fint.altinn.model.ebevis.Authorization
import no.fint.altinn.model.ebevis.ErrorCode
import no.fint.altinn.model.ebevis.EvidenceCode
import no.novari.ebevis.client.DataAltinnClient
import no.novari.ebevis.configuration.MongoConfiguration
import no.novari.ebevis.exception.AltinnException
import no.novari.ebevis.repository.AltinnApplicationRepository
import no.novari.ebevis.service.AccreditationService
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
class AccreditationServiceSpec extends Specification {
    DataAltinnClient client = Mock()

    @Autowired
    AltinnApplicationRepository repository

    AccreditationService service

    void setup() {
        service = new AccreditationService(client, repository)
    }

    void cleanup() {
        repository.deleteAll()
    }

    def "createAccreditations updates application with new data and status"() {
        given:
        repository.save(new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', status: AltinnApplicationStatus.NEW))

        def accreditations = service.createAccreditations()

        1 * client.createAccreditation(_ as Authorization) >> Mono.just(newAccreditation('1', '2000-01-01T00:00:00Z'))

        expect:
        StepVerifier.create(accreditations)
                .expectNextMatches({ application ->
                    application.accreditationId == '1' &&
                            application.accreditationDate == OffsetDateTime.parse('2000-01-01T00:00:00Z') &&
                            application.accreditationCount == 0 &&
                            application.consents.get('KonkursDrosje').status == ConsentStatus.CONSENT_REQUESTED &&
                            application.consents.get('RestanserDrosje').status == ConsentStatus.CONSENT_REQUESTED &&
                            application.status == AltinnApplicationStatus.CONSENTS_REQUESTED
                })
                .verifyComplete()

        def application = repository.findById('1')
        application.isPresent()
        application.get().accreditationId == '1'
        application.get().accreditationDate == OffsetDateTime.parse('2000-01-01T00:00:00Z')
        application.get().accreditationCount == 0
        application.get().consents.get('KonkursDrosje').status == ConsentStatus.CONSENT_REQUESTED
        application.get().consents.get('RestanserDrosje').status == ConsentStatus.CONSENT_REQUESTED
        application.get().status == AltinnApplicationStatus.CONSENTS_REQUESTED
    }

    def "createAccreditations continues on error"() {
        given:
        repository.saveAll([new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', status: AltinnApplicationStatus.NEW),
                            new AltinnApplication(requestor: '2', subject: '2', archiveReference: '2', status: AltinnApplicationStatus.NEW)])

        def accreditations = service.createAccreditations()

        2 * client.createAccreditation(_ as Authorization) >>> [Mono.error(new AltinnException(HttpStatus.INTERNAL_SERVER_ERROR, new ErrorCode(code: 1003, description: 'description'))),
                                                                Mono.just(newAccreditation('1', '2000-01-01T00:00:00Z'))]

        expect:
        StepVerifier.create(accreditations)
                .expectNextCount(1)
                .verifyComplete()
    }

    def "createAccreditations updates application on specific error codes"() {
        given:
        repository.save(new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', status: AltinnApplicationStatus.NEW))

        def accreditations = service.createAccreditations()

        1 * client.createAccreditation(_ as Authorization) >> Mono.error(new AltinnException(HttpStatus.INTERNAL_SERVER_ERROR, new ErrorCode(code: 1004, description: 'description')))

        expect:
        StepVerifier.create(accreditations)
                .expectNextMatches({ application ->
                    application.status == AltinnApplicationStatus.CONSENTS_INVALID_SUBJECT
                })
                .verifyComplete()
    }

    def newAccreditation(String id, String date) {
        return new Accreditation(id: id,
                issued: OffsetDateTime.parse(date),
                evidenceCodes: [new EvidenceCode(evidenceCodeName: 'KonkursDrosje'),
                                new EvidenceCode(evidenceCodeName: 'RestanserDrosje')])
    }
}