package no.fint.ebevis.repository

import no.fint.altinn.model.AltinnApplication
import no.fint.altinn.model.AltinnApplicationStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import spock.lang.Specification

@DataMongoTest
class AltinnApplicationRepositorySpec extends Specification {

    @Autowired
    AltinnApplicationRepository repository

    def "findAllByStatus() returns documents given status"() {
        given:
        repository.saveAll(Arrays.asList(new AltinnApplication(status: AltinnApplicationStatus.NEW),
                new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_REQUESTED),
                new AltinnApplication(status: AltinnApplicationStatus.CONSENTS_REQUESTED)))

        when:
        def documents = repository.findAllByStatus(AltinnApplicationStatus.NEW)

        then:
        documents.size() == 1
    }

    def "findAllByStatusInAndAccreditationIdIn() returns documents given status"() {
        given:
        repository.saveAll(Arrays.asList(
                new AltinnApplication(accreditationId: 'id1', status: AltinnApplicationStatus.CONSENTS_REQUESTED),
                new AltinnApplication(accreditationId: 'id2', status: AltinnApplicationStatus.CONSENTS_ACCEPTED),
                new AltinnApplication(accreditationId: 'id3', status: AltinnApplicationStatus.ARCHIVED),
                new AltinnApplication(accreditationId: 'id4', status: AltinnApplicationStatus.PURGED)))

        when:
        def documents = repository.findAllByStatusInAndAccreditationIdIn([AltinnApplicationStatus.CONSENTS_REQUESTED, AltinnApplicationStatus.CONSENTS_ACCEPTED], ['id1','id2'])

        then:
        documents.size() == 2
        documents.get(0).accreditationId == 'id1'
        documents.get(1).accreditationId == 'id2'
    }
}