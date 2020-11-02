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
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsentService {
    private final DataAltinnClient client;
    private final AltinnApplicationRepository repository;

    private OffsetDateTime lastUpdated = OffsetDateTime.parse("1970-01-01T00:00:00Z");

    public ConsentService(DataAltinnClient client, AltinnApplicationRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay}", fixedDelayString = "${scheduling.fixed-delay}")
    public void run() {
        createAccreditations();

        updateStatuses();

        sendReminders();
    }

    public void createAccreditations() {
        List<AltinnApplication> applications = repository.findAllByStatus(AltinnApplicationStatus.NEW);

        log.info("Found {} new application(s).", applications.size());

        Flux.fromIterable(applications)
                .delayElements(Duration.ofSeconds(1))
                .subscribe(this::createAccreditation);
    }

    public void createAccreditation(AltinnApplication application) {
        Authorization authorization = ConsentFactory.ofTaxiLicenseApplication(application);

        client.createAccreditation(authorization)
                .doOnSuccess(entity -> {
                    Accreditation accreditation = entity.getBody();

                    if (accreditation == null) {
                        return;
                    }

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

                    repository.save(application);
                })
                .doOnError(WebClientResponseException.class, ex -> log.error("Accreditation of archive reference: {} - {}", application.getArchiveReference(), ex.getResponseBodyAsString()))
                .subscribe();
    }

    public void updateStatuses() {
        client.getAccreditations(lastUpdated)
                .doOnSuccess(accreditations -> {
                    List<String> ids = accreditations.stream().map(Accreditation::getId).collect(Collectors.toList());

                    List<AltinnApplication> applications =
                            repository.findAllByStatusInAndAccreditationIdIn(Arrays.asList(AltinnApplicationStatus.CONSENTS_REQUESTED, AltinnApplicationStatus.CONSENTS_ACCEPTED), ids);

                    log.info("Found {} application(s) with new consent status since {}.", applications.size(), lastUpdated.toString());

                    Flux.fromIterable(applications)
                            .delayElements(Duration.ofSeconds(1))
                            .doOnComplete(() -> lastUpdated = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC))
                            .subscribe(this::updateStatus);
                })
                .doOnError(WebClientResponseException.class, ex -> log.error(ex.getResponseBodyAsString()))
                .subscribe();
    }

    public void updateStatus(AltinnApplication application) {
        client.getEvidenceStatuses(application.getAccreditationId())
                .doOnSuccess(statuses -> {
                    updateConsentStatuses(application, statuses);
                    repository.save(application);
                })
                .doOnError(WebClientResponseException.class, ex -> log.error(ex.getResponseBodyAsString()))
                .subscribe();
    }

    public void sendReminders() {
        List<AltinnApplication> applications = repository.findAllByStatus(AltinnApplicationStatus.CONSENTS_REQUESTED);

        List<AltinnApplication> reminders = applications.stream()
                .filter(application -> {
                    Duration between = Duration.between(application.getAccreditationDate(), OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));

                    return (between.toDays() > 7 && application.getAccreditationCount() <= 3);
                }).collect(Collectors.toList());

        log.info("Found {} application(s) ready for reminders.", reminders.size());

        Flux.fromIterable(reminders)
                .delayElements(Duration.ofSeconds(1))
                .subscribe(this::sendReminder);
    }

    public void sendReminder(AltinnApplication application) {
        client.createReminder(application.getAccreditationId())
                .doOnSuccess(entity -> {
                    Notification notification = entity.getBody();

                    if (notification == null) {
                        return;
                    }

                    application.setAccreditationDate(notification.getDate());
                    application.setAccreditationCount(notification.getRecipientCount());
                    repository.save(application);
                })
                .doOnError(WebClientResponseException.class, ex -> log.error("Reminder of archive reference: {} - {}", application.getArchiveReference(), ex.getResponseBodyAsString()))
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

        if (isAccepted.test(consents)) {
            application.setStatus(AltinnApplicationStatus.CONSENTS_ACCEPTED);
            return;
        }

        if (isCanceled.test(consents)) {
            application.setStatus(AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED);
        }
    }

    private final Predicate<Collection<AltinnApplication.Consent>> isAccepted = consents ->
            !consents.isEmpty() && consents.stream().map(AltinnApplication.Consent::getStatus).allMatch(ConsentStatus.CONSENT_ACCEPTED::equals);

    private final Predicate<Collection<AltinnApplication.Consent>> isCanceled = consents ->
            !consents.isEmpty() && consents.stream().map(AltinnApplication.Consent::getStatus).anyMatch(status ->
                    status.equals(ConsentStatus.CONSENT_REJECTED) || status.equals(ConsentStatus.CONSENT_EXPIRED));
}