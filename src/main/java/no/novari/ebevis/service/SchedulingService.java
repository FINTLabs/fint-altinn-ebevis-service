package no.novari.ebevis.service;

import no.novari.ebevis.kafka.ConsentAcceptedPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulingService {
    private final AccreditationService accreditationService;
    private final EvidenceService evidenceService;
    private final ReminderService reminderService;
    private final ExpiredService expiredService;
    private final ConsentAcceptedPublisher consentAcceptedPublisher;

    public SchedulingService(AccreditationService accreditationService, EvidenceService evidenceService, ReminderService reminderService, ExpiredService expiredService, ConsentAcceptedPublisher consentAcceptedPublisher) {
        this.accreditationService = accreditationService;
        this.evidenceService = evidenceService;
        this.reminderService = reminderService;
        this.expiredService = expiredService;
        this.consentAcceptedPublisher = consentAcceptedPublisher;
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay}", fixedDelayString = "${scheduling.fixed-delay}")
    public void run() {
        accreditationService.createAccreditations()
                .concatWith(evidenceService.updateEvidence())
                .concatWith(consentAcceptedPublisher.sendAcceptedConsents())
                .concatWith(reminderService.sendReminders())
                .concatWith(expiredService.setExpired())
                .subscribe();
    }
}
