package no.fint.ebevis.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulingService {
    private final AccreditationService accreditationService;
    private final EvidenceService evidenceService;
    private final ReminderService reminderService;

    public SchedulingService(AccreditationService accreditationService, EvidenceService evidenceService, ReminderService reminderService) {
        this.accreditationService = accreditationService;
        this.evidenceService = evidenceService;
        this.reminderService = reminderService;
    }

    @Scheduled(initialDelayString = "${scheduling.initial-delay}", fixedDelayString = "${scheduling.fixed-delay}")
    public void run() {
        accreditationService.createAccreditations()
                .concatWith(evidenceService.updateEvidence())
                .concatWith(reminderService.sendReminders())
                .subscribe();
    }
}
