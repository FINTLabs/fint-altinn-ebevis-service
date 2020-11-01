package no.fint.ebevis.repository

import no.fint.ebevis.model.AltinnApplication
import no.fint.ebevis.model.AltinnApplicationStatus
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

    def "findAllByStatusAndAccreditationIdIn() returns documents given status"() {
        given:
        repository.saveAll(Arrays.asList(new AltinnApplication(accreditationId: 'id1', status: AltinnApplicationStatus.CONSENTS_REQUESTED),
                new AltinnApplication(accreditationId: 'id2', status: AltinnApplicationStatus.CONSENTS_ACCEPTED)))

        when:
        def documents = repository.findAllByStatusAndAccreditationIdIn(AltinnApplicationStatus.CONSENTS_REQUESTED, ['id1'])

        then:
        documents.size() == 1
        documents.get(0).accreditationId == 'id1'
    }
}