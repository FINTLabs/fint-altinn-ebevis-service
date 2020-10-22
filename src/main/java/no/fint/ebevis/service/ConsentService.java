package no.fint.ebevis.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.factory.ConsentFactory;
import no.fint.ebevis.model.AltinnApplication;
import no.fint.ebevis.model.AltinnApplicationConsentStatus;
import no.fint.ebevis.model.AltinnApplicationStatus;
import no.fint.ebevis.model.ebevis.*;
import no.fint.ebevis.repository.AltinnApplicationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
public class ConsentService {
    private final DataAltinnClient dataAltinnClient;
    private final AltinnApplicationRepository altinnApplicationRepository;

    public ConsentService(DataAltinnClient dataAltinnClient, AltinnApplicationRepository altinnApplicationRepository) {
        this.dataAltinnClient = dataAltinnClient;
        this.altinnApplicationRepository = altinnApplicationRepository;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 10000000)
    public void init() {
        AltinnApplication altinnApplication1 = new AltinnApplication();
        altinnApplication1.setRequestor("921693230");
        altinnApplication1.setSubject("998997801");
        altinnApplication1.setArchiveReference("ArchiveReference1");
        altinnApplication1.setStatus(AltinnApplicationStatus.NEW);
        altinnApplicationRepository.save(altinnApplication1);

        AltinnApplication altinnApplication2 = new AltinnApplication();
        altinnApplication2.setRequestor("921693230");
        altinnApplication2.setSubject("998997801");
        altinnApplication2.setArchiveReference("ArchiveReference2");
        altinnApplication2.setStatus(AltinnApplicationStatus.NEW);

        altinnApplicationRepository.save(altinnApplication2);
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay}", fixedDelayString = "${scheduling.fixed-delay}")
    public void run() {
        consentNew();

        consentStatus();

        evidence();
    }

    public void consentNew() {
        List<AltinnApplication> altinnApplications = altinnApplicationRepository.findByStatus(AltinnApplicationStatus.NEW);

        if (altinnApplications.isEmpty()) {
            return;
        }

        altinnApplications.forEach(altinnApplication -> {
            Authorization authorization = ConsentFactory.ofTaxiLicenseApplication(Integer.parseInt(altinnApplication.getRequestor()), Integer.parseInt(altinnApplication.getSubject()), altinnApplication.getArchiveReference());

            dataAltinnClient.createAccreditation(authorization)
                    .doOnSuccess(responseEntity -> {
                        Accreditation accreditation = responseEntity.getBody();

                        if (accreditation == null) {
                            return;
                        }

                        altinnApplication.setAccreditationId(accreditation.getId());

                        altinnApplication.setStatus(AltinnApplicationStatus.CONSENT_REQUESTED);

                        altinnApplicationRepository.save(altinnApplication);
                    })
                    .doOnError(WebClientResponseException.class, webClientResponseException ->
                            log.error(webClientResponseException.getResponseBodyAsString(), webClientResponseException))
                    .block();
        });
    }

    public void consentStatus() {
        List<AltinnApplication> altinnApplications = altinnApplicationRepository.findByStatus(AltinnApplicationStatus.CONSENT_REQUESTED);

        if (altinnApplications.isEmpty()) {
            return;
        }

        altinnApplications.forEach(altinnApplication -> dataAltinnClient.getEvidenceStatuses(altinnApplication.getAccreditationId())
                .doOnSuccess(evidenceStatuses -> evidenceStatuses.forEach(evidenceStatus -> Optional.ofNullable(evidenceStatus)
                        .map(EvidenceStatus::getStatus)
                        .map(EvidenceStatusCode::getCode)
                        .ifPresent(code -> {
                            AltinnApplicationConsentStatus altinnApplicationStatus;

                            switch (code) {
                                case 1:
                                    altinnApplicationStatus = AltinnApplicationConsentStatus.CONSENT_ACCEPTED;
                                    break;
                                case 2:
                                    altinnApplicationStatus = AltinnApplicationConsentStatus.CONSENT_REQUESTED;
                                    break;
                                case 3:
                                    altinnApplicationStatus = AltinnApplicationConsentStatus.CONSENT_REJECTED;
                                    break;
                                case 4:
                                    altinnApplicationStatus = AltinnApplicationConsentStatus.CONSENT_EXPIRED;
                                    break;
                                default:
                                    altinnApplicationStatus = AltinnApplicationConsentStatus.AWAITING_DATA_FROM_SOURCE;
                                    break;
                            }

                            altinnApplication.getConsents().put(evidenceStatus.getEvidenceCodeName(), altinnApplicationStatus);

                            altinnApplicationRepository.save(altinnApplication);
                        })))
                .doOnError(WebClientResponseException.class, webClientResponseException ->
                        log.error(webClientResponseException.getResponseBodyAsString(), webClientResponseException))
                .block());
    }

    public void evidence() {
        List<AltinnApplication> altinnApplications = altinnApplicationRepository.findByStatus(AltinnApplicationStatus.CONSENT_REQUESTED);

        if (altinnApplications.isEmpty()) {
            return;
        }

        altinnApplications.stream().filter(hasAcceptedAllConsents)
                .forEach(altinnApplication -> Flux.fromIterable(altinnApplication.getConsents().keySet())
                        .flatMap(evidenceCodeName -> dataAltinnClient.getEvidence(altinnApplication.getAccreditationId(), evidenceCodeName))
                        .collectList()
                        .doOnSuccess(evidence -> {
                            evidence.forEach(altinnApplication.getEvidence()::add);

                            altinnApplication.setStatus(AltinnApplicationStatus.EVIDENCE_FETCHED);
                            altinnApplicationRepository.save(altinnApplication);
                        })
                        .doOnError(WebClientResponseException.class, webClientResponseException ->
                                log.error(webClientResponseException.getResponseBodyAsString(), webClientResponseException))
                        .block());
    }

    private final Predicate<AltinnApplication> hasAcceptedAllConsents = altinnApplication -> {
        Collection<AltinnApplicationConsentStatus> consentStatuses = altinnApplication.getConsents().values();

        return !consentStatuses.isEmpty() && consentStatuses.stream().allMatch(AltinnApplicationConsentStatus.CONSENT_ACCEPTED::equals);
    };
}