package no.fint.ebevis.repository;

import no.fint.ebevis.model.TaxiLicenseApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxiLicenseApplicationRepository extends MongoRepository<TaxiLicenseApplication, String> {
}
