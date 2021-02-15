package no.fint.ebevis.service

import no.fint.altinn.model.AltinnApplication
import no.fint.altinn.model.AltinnApplicationStatus
import no.fint.ebevis.configuration.MongoConfiguration
import no.fint.ebevis.repository.AltinnApplicationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.LocalDateTime

@DataMongoTest
@Import(MongoConfiguration.class)
class ExpiredServiceSpec extends Specification {

    @Autowired
    AltinnApplicationRepository repository

    ExpiredService service

    void setup() {
        service = new ExpiredService(repository)
    }

    void cleanup() {
        repository.deleteAll()
    }

    def "setExpired updates applications with expired consents with new status"() {
        given:
        repository.saveAll([new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', archivedDate: LocalDateTime.now().minusDays(90), status: AltinnApplicationStatus.CONSENTS_REQUESTED),
                            new AltinnApplication(requestor: '2', subject: '2', archiveReference: '2', archivedDate: LocalDateTime.now().minusDays(89), status: AltinnApplicationStatus.CONSENTS_REQUESTED),
                            new AltinnApplication(requestor: '3', subject: '3', archiveReference: '3', archivedDate: LocalDateTime.now().minusDays(91), status: AltinnApplicationStatus.CONSENTS_ACCEPTED),
                            new AltinnApplication(requestor: '4', subject: '4', archiveReference: '4', archivedDate: LocalDateTime.now().minusDays(10), status: AltinnApplicationStatus.CONSENTS_ACCEPTED)])

        when:
        def expired = service.setExpired()

        then:
        StepVerifier.create(expired)
                .expectNextMatches({ application ->
                    application.status == AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED &&
                            application.archiveReference == '1'
                })
                .expectNextMatches({ application ->
                    application.status == AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED &&
                            application.archiveReference == '3'
                })
                .verifyComplete()

        def applications = repository.findAllByStatus(AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED)
        applications.size() == 2
        applications.first().archiveReference == '1'
        applications.first().status == AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED
        applications.last().archiveReference == '3'
        applications.last().status == AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED
    }
}
