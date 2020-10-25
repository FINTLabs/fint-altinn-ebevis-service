package no.fint.ebevis.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.factory.ConsentFactory;
import no.fint.ebevis.model.AltinnApplication;
import no.fint.ebevis.model.ConsentStatus;
import no.fint.ebevis.model.AltinnApplicationStatus;
import no.fint.ebevis.model.ebevis.*;
import no.fint.ebevis.repository.AltinnApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
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
        checkOnConsentStatuses();

        checkForNewApplications();

        gatherEvidence();
    }

    public void checkForNewApplications() {
        List<AltinnApplication> applications = repository.findAllByStatus(AltinnApplicationStatus.NEW);

        log.info("Found {} new applications.", applications.size());

        applications.forEach(application -> {
            Authorization authorization = ConsentFactory.ofTaxiLicenseApplication(application.getRequestor(), application.getSubject(), application.getArchiveReference());

            client.createAccreditation(authorization)
                    .doOnSuccess(entity -> {
                        Accreditation accreditation = entity.getBody();

                        if (accreditation == null) {
                            return;
                        }

                        application.setStatus(AltinnApplicationStatus.CONSENTS_REQUESTED);

                        AltinnApplication.Consent consent = new AltinnApplication.Consent();
                        consent.setId(accreditation.getId());

                        application.setConsent(consent);

                        repository.save(application);
                    })
                    .doOnError(WebClientResponseException.class, ex -> log.error(ex.getResponseBodyAsString()))
                    .onErrorResume(it -> Mono.empty())
                    .block();
        });
    }

    public void checkOnConsentStatuses() {
        List<String> ids;

        try {
            ids = client.getAccreditations(lastUpdated)
                    .blockOptional()
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(Accreditation::getId)
                    .collect(Collectors.toList());
        } catch (WebClientResponseException ex) {
            log.error(ex.getResponseBodyAsString());
            return;
        }

        List<AltinnApplication> applications = repository.findAllByConsentIdIn(ids);

        log.info("Found {} applications with new consent status since {}.", applications.size(), lastUpdated.toString());

        applications.stream()
                .peek(application -> client.getEvidenceStatuses(application.getConsent().getId())
                        .doOnSuccess(statuses -> updateConsentStatuses(application, statuses))
                        .doOnError(WebClientResponseException.class, ex -> log.error(ex.getResponseBodyAsString()))
                        .onErrorResume(it -> Mono.empty())
                        .block())
                .forEach(repository::save);

        lastUpdated = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    }

    public void gatherEvidence() {
        List<AltinnApplication> applications = repository.findAllByStatus(AltinnApplicationStatus.CONSENTS_ACCEPTED);

        log.info("Found {} applications with all consents accepted.", applications.size());

        applications.forEach(application -> Flux.fromIterable(application.getConsent().getStatus().keySet())
                .flatMap(evidenceCodeName -> client.getEvidence(application.getConsent().getId(), evidenceCodeName))
                .collectList()
                .doOnSuccess(evidence -> {
                    evidence.forEach(application.getConsent().getEvidence()::add);

                    application.setStatus(AltinnApplicationStatus.EVIDENCE_FETCHED);

                    repository.save(application);
                })
                .doOnError(WebClientResponseException.class, ex -> log.error(ex.getResponseBodyAsString()))
                .onErrorResume(it -> Mono.empty())
                .block());
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

                    application.getConsent().getStatus().put(status.getEvidenceCodeName(), consentStatus);
                })
        );

        Collection<ConsentStatus> consentStatuses = application.getConsent().getStatus().values();

        if (!consentStatuses.isEmpty() && consentStatuses.stream().allMatch(ConsentStatus.CONSENT_ACCEPTED::equals)) {
            application.setStatus(AltinnApplicationStatus.CONSENTS_ACCEPTED);
        }
    }
}