package no.fint.ebevis.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import no.fint.altinn.model.ConsentStatus;
import no.fint.altinn.model.ebevis.*;
import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.exception.AltinnException;
import no.fint.ebevis.repository.AltinnApplicationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EvidenceService {
    private final DataAltinnClient client;
    private final AltinnApplicationRepository repository;

    private OffsetDateTime lastUpdated = OffsetDateTime.parse("1970-01-01T00:00:00Z");

    public EvidenceService(DataAltinnClient client, AltinnApplicationRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public Flux<AltinnApplication> updateEvidence() {
        return client.getAccreditations(lastUpdated)
                .flatMapIterable(accreditations -> {
                    List<String> ids = accreditations.stream().map(Accreditation::getId).collect(Collectors.toList());

                    List<AltinnApplication> applications = repository.findAllByStatusInAndAccreditationIdIn(Arrays.asList(AltinnApplicationStatus.CONSENTS_REQUESTED, AltinnApplicationStatus.CONSENTS_ACCEPTED), ids);

                    log.info("Found {} application(s) with new consent status since {}.", applications.size(), lastUpdated.toString());

                    lastUpdated = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);

                    return applications;
                })
                .delayElements(Duration.ofMillis(1000))
                .flatMap(this::update)
                .onErrorContinue((a, b) -> {
                })
                .doOnNext(repository::save);
    }

    private Mono<AltinnApplication> update(AltinnApplication application) {
        return client.getEvidenceStatuses(application.getAccreditationId())
                .map(evidenceStatuses -> updateEvidenceStatus(application, evidenceStatuses))
                .doOnError(AltinnException.class, ex -> {
                    log.error("Status of archive reference: {} - {}", application.getArchiveReference(), ex.getErrorCode());

                    lastUpdated = OffsetDateTime.parse("1970-01-01T00:00:00Z");
                });
    }

    private AltinnApplication updateEvidenceStatus(AltinnApplication application, List<EvidenceStatus> statuses) {
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

                    application.getConsents().computeIfPresent(status.getEvidenceCodeName(), (key, value) -> {
                        value.setStatus(consentStatus);
                        return value;
                    });
                })
        );

        Collection<AltinnApplication.Consent> consents = application.getConsents().values();

        if (isAccepted.test(consents)) {
            application.setStatus(AltinnApplicationStatus.CONSENTS_ACCEPTED);
        }

        if (isCanceled.test(consents)) {
            application.setStatus(AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED);
        }

        return application;
    }

    private final Predicate<Collection<AltinnApplication.Consent>> isAccepted = consents ->
            !consents.isEmpty() && (consents.stream().map(AltinnApplication.Consent::getStatus).allMatch(ConsentStatus.CONSENT_ACCEPTED::equals) ||
                    consents.stream().map(AltinnApplication.Consent::getDocumentId).allMatch(Objects::nonNull));

    private final Predicate<Collection<AltinnApplication.Consent>> isCanceled = consents ->
            !consents.isEmpty() && consents.stream().map(AltinnApplication.Consent::getStatus).anyMatch(status ->
                    status.equals(ConsentStatus.CONSENT_REJECTED) || status.equals(ConsentStatus.CONSENT_EXPIRED));
}