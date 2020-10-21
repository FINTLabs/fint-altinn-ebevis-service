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

    def "findByStatus() returns documents given status"() {
        given:
        repository.saveAll(Arrays.asList(new AltinnApplication(status: AltinnApplicationStatus.NEW),
                new AltinnApplication(status: AltinnApplicationStatus.CONSENT_REQUESTED),
                new AltinnApplication(status: AltinnApplicationStatus.CONSENT_REQUESTED)))

        when:
        def documents = repository.findByStatus(AltinnApplicationStatus.NEW)

        then:
        documents.size() == 1
    }
}