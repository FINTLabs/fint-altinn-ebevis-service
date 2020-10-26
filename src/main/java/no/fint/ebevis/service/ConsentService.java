package no.fint.ebevis.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.factory.ConsentFactory;
import no.fint.ebevis.model.AltinnApplication;
import no.fint.ebevis.model.ConsentStatus;
import no.fint.ebevis.model.AltinnApplicationStatus;
import no.fint.ebevis.model.ebevis.*;
import no.fint.ebevis.repository.AltinnApplicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsentService {
    private final DataAltinnClient client;
    private final AltinnApplicationRepository repository;

    private OffsetDateTime lastUpdated = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);

    public ConsentService(DataAltinnClient client, AltinnApplicationRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay}", fixedDelayString = "${scheduling.fixed-delay}")
    public void run() {
        checkForNewApplications();

        checkForNewConsentStatuses();
    }

    public void checkForNewApplications() {
        List<AltinnApplication> applications = repository.findAllByStatus(AltinnApplicationStatus.NEW);

        log.info("Found {} new application(s).", applications.size());

        applications.forEach(application -> {
            Authorization authorization = ConsentFactory.ofTaxiLicenseApplication(application.getRequestor(), application.getSubject(), application.getArchiveReference());

            client.createAccreditation(authorization)
                    .doOnSuccess(entity -> {
                        Accreditation accreditation = entity.getBody();

                        if (accreditation == null) {
                            return;
                        }

                        application.setStatus(AltinnApplicationStatus.CONSENTS_REQUESTED);

                        application.setAccreditationId(accreditation.getId());

                        accreditation.getEvidenceCodes().forEach(evidenceCode -> {
                            AltinnApplication.Consent consent = new AltinnApplication.Consent();
                            consent.setStatus(ConsentStatus.CONSENT_REQUESTED);
                            consent.setEvidenceCodeName(evidenceCode.getEvidenceCodeName());
                            application.getConsents().put(evidenceCode.getEvidenceCodeName(), consent);
                        });

                        repository.save(application);
                    })
                    .doOnError(WebClientResponseException.class, ex -> log.error(ex.getResponseBodyAsString()))
                    .subscribe();
        });
    }

    public void checkForNewConsentStatuses() {
        client.getAccreditations(lastUpdated)
                .doOnSuccess(accreditations -> {
                    List<String> ids = accreditations.stream().map(Accreditation::getId).collect(Collectors.toList());

                    List<AltinnApplication> applications = repository.findAllByAccreditationIdIn(ids);

                    log.info("Found {} application(s) with new consent status since {}.", applications.size(), lastUpdated.toString());

                    lastUpdated = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);

                    applications.forEach(application -> client.getEvidenceStatuses(application.getAccreditationId())
                            .doOnSuccess(statuses -> {
                                updateConsentStatuses(application, statuses);
                                repository.save(application);
                            })
                            .doOnError(WebClientResponseException.class, ex -> log.error(ex.getResponseBodyAsString()))
                            .subscribe());
                })
                .doOnError(WebClientResponseException.class, ex -> log.error(ex.getResponseBodyAsString()))
                .subscribe();
    }

    private void updateConsentStatuses(AltinnApplication application, List<EvidenceStatus> statuses) {
        statuses.forEach(status -> Optional.ofNullable(status)
                .map(EvidenceStatus::getStatus)
                .map(EvidenceStatusCode::getCode)
                .ifPresent(code -> {
                    ConsentStatus consentStatus;

                    switch (code) {
                        case 1:
                            consentStatus = ConsentStatus.CONSENT_ACCEPTED;
                            break;
                        case 2:
                            consentStatus = ConsentStatus.CONSENT_REQUESTED;
                            break;
                        case 3:
                            consentStatus = ConsentStatus.CONSENT_REJECTED;
                            break;
                        case 4:
                            consentStatus = ConsentStatus.CONSENT_EXPIRED;
                            break;
                        default:
                            consentStatus = ConsentStatus.AWAITING_DATA_FROM_SOURCE;
                            break;
                    }


                    application.getConsents().get(status.getEvidenceCodeName()).setStatus(consentStatus);
                })
        );

        Collection<AltinnApplication.Consent> consents = application.getConsents().values();

        if (!consents.isEmpty() && consents.stream().map(AltinnApplication.Consent::getStatus).allMatch(ConsentStatus.CONSENT_ACCEPTED::equals)) {
            application.setStatus(AltinnApplicationStatus.CONSENTS_ACCEPTED);
        }
    }
}