package no.fint.ebevis.repository;

import no.fint.ebevis.model.AltinnApplication;
import no.fint.ebevis.model.AltinnApplicationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AltinnApplicationRepository extends MongoRepository<AltinnApplication, String> {

    List<AltinnApplication> findAllByStatus(AltinnApplicationStatus status);

    List<AltinnApplication> findAllByConsentIdIn(List<String> ids);
}
