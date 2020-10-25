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

    def "findAllByConsentIdIn() returns documents given status"() {
        given:
        repository.saveAll(Arrays.asList(new AltinnApplication(consent: new AltinnApplication.Consent(id: 'id1')),
                new AltinnApplication(consent: new AltinnApplication.Consent(id: 'id2'))))

        when:
        def documents = repository.findAllByConsentIdIn(['id1'])

        then:
        documents.size() == 1
    }
}