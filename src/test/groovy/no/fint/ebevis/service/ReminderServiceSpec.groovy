package no.fint.ebevis.service

import no.fint.altinn.model.AltinnApplication
import no.fint.altinn.model.AltinnApplicationStatus
import no.fint.altinn.model.ebevis.ErrorCode
import no.fint.altinn.model.ebevis.Notification
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
class ReminderServiceSpec extends Specification {
    DataAltinnClient client = Mock()

    @Autowired
    AltinnApplicationRepository repository

    ReminderService service

    void setup() {
        service = new ReminderService(client, repository)
    }

    void cleanup() {
        repository.deleteAll()
    }

    def "createReminders updates application with new data"() {
        given:
        repository.save(new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                accreditationId: '1', accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z'), accreditationCount: 0))

        def reminders = service.sendReminders()

        1 * client.createReminder(_ as String) >> Mono.just([new Notification(success: true, date: OffsetDateTime.parse('2000-01-10T00:00:00Z'))])

        expect:
        StepVerifier.create(reminders)
                .expectNextMatches({ application ->
                    application.accreditationId == '1' &&
                            application.accreditationDate == OffsetDateTime.parse('2000-01-10T00:00:00Z') &&
                            application.accreditationCount == 1 &&
                            application.status == AltinnApplicationStatus.CONSENTS_REQUESTED
                })
                .verifyComplete()

        def application = repository.findById('1')
        application.isPresent()
        application.get().accreditationId == '1'
        application.get().accreditationDate == OffsetDateTime.parse('2000-01-10T00:00:00Z')
        application.get().accreditationCount == 1
        application.get().status == AltinnApplicationStatus.CONSENTS_REQUESTED
    }

    def "createReminders continues on error"() {
        given:
        repository.saveAll([new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                accreditationId: '1', accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z'), accreditationCount: 0),
                            new AltinnApplication(requestor: '2', subject: '2', archiveReference: '2', status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                                    accreditationId: '2', accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z'), accreditationCount: 0)])

        def reminders = service.sendReminders()

        2 * client.createReminder(_ as String) >>> [Mono.error(new AltinnException(HttpStatus.INTERNAL_SERVER_ERROR, new ErrorCode(code: 1003, description: 'description'))),
                                                    Mono.just([new Notification(success: true, date: OffsetDateTime.parse('2000-01-10T00:00:00Z'))])]

        expect:
        StepVerifier.create(reminders)
                .expectNextCount(1)
                .verifyComplete()
    }

    def "createReminders updates application on specific error codes"() {
        given:
        repository.save(new AltinnApplication(requestor: '1', subject: '1', archiveReference: '1', status: AltinnApplicationStatus.CONSENTS_REQUESTED,
                accreditationId: '1', accreditationDate: OffsetDateTime.parse('2000-01-01T00:00:00Z'), accreditationCount: 0))

        def reminders = service.sendReminders()

        1 * client.createReminder(_ as String) >> Mono.error(new AltinnException(HttpStatus.INTERNAL_SERVER_ERROR, new ErrorCode(code: 1019, description: 'description')))

        expect:
        StepVerifier.create(reminders)
                .expectNextMatches({ application ->
                    application.accreditationDate > OffsetDateTime.parse('2000-01-01T00:00:00Z')
                })
                .verifyComplete()
    }
}
