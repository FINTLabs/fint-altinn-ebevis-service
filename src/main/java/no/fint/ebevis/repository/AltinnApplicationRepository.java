package no.fint.ebevis.repository;

import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AltinnApplicationRepository extends MongoRepository<AltinnApplication, String> {
    List<AltinnApplication> findAllByStatus(AltinnApplicationStatus status);

    List<AltinnApplication> findAllByStatusInAndAccreditationIdIn(List<AltinnApplicationStatus> statuses, List<String> ids);
}
