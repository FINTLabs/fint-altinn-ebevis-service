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

    def "findAllByAccreditationIdIn() returns documents given status"() {
        given:
        repository.saveAll(Arrays.asList(new AltinnApplication(accreditationId: 'id1'), new AltinnApplication(accreditationId: 'id2')))

        when:
        def documents = repository.findAllByAccreditationIdIn(['id1'])

        then:
        documents.size() == 1
    }
}