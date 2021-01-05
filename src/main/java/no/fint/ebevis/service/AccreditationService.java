package no.fint.ebevis.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import no.fint.altinn.model.ConsentStatus;
import no.fint.altinn.model.ebevis.Authorization;
import no.fint.altinn.model.ebevis.ErrorCode;
import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.exception.AltinnException;
import no.fint.ebevis.factory.ConsentFactory;
import no.fint.ebevis.repository.AltinnApplicationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AccreditationService {
    private final DataAltinnClient client;
    private final AltinnApplicationRepository repository;

    public AccreditationService(DataAltinnClient client, AltinnApplicationRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public Flux<AltinnApplication> createAccreditations() {
        List<AltinnApplication> applications = repository.findAllByStatus(AltinnApplicationStatus.NEW);

        log.info("Found {} new application(s).", applications.size());

        return Flux.fromIterable(applications)
                .delayElements(Duration.ofMillis(1000))
                .flatMap(this::create)
                .onErrorContinue((a, b) -> {
                })
                .doOnNext(repository::save);
    }

    private Mono<AltinnApplication> create(AltinnApplication application) {
        Authorization authorization = ConsentFactory.ofTaxiLicenseApplication(application);

        return client.createAccreditation(authorization)
                .map(accreditation -> {
                    application.setStatus(AltinnApplicationStatus.CONSENTS_REQUESTED);
                    application.setAccreditationId(accreditation.getId());
                    application.setAccreditationDate(accreditation.getIssued());
                    application.setAccreditationCount(0);

                    accreditation.getEvidenceCodes()
                            .stream()
                            .map(evidenceCode -> {
                                AltinnApplication.Consent consent = new AltinnApplication.Consent();
                                consent.setStatus(ConsentStatus.CONSENT_REQUESTED);
                                consent.setEvidenceCodeName(evidenceCode.getEvidenceCodeName());

                                return consent;
                            })
                            .forEach(consent -> application.getConsents().put(consent.getEvidenceCodeName(), consent));

                    return application;
                })
                .onErrorResume(AltinnException.class, altinnException -> altinnExceptionHandler(application, altinnException));
    }

    private Mono<AltinnApplication> altinnExceptionHandler(AltinnApplication application, AltinnException altinnException) {
        log.error("Accreditation of archive reference: {} - {}", application.getArchiveReference(), altinnException.getErrorCode());

        return Optional.of(altinnException)
                .map(AltinnException::getErrorCode)
                .map(ErrorCode::getCode)
                .filter(code -> code == 1004)
                .map(code -> {
                    application.setStatus(AltinnApplicationStatus.CONSENTS_INVALID_SUBJECT);

                    return Mono.just(application);
                })
                .orElse(Mono.error(altinnException));
    }
}