package no.fint.ebevis.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import no.fint.altinn.model.ebevis.ErrorCode;
import no.fint.altinn.model.ebevis.Notification;
import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.exception.AltinnException;
import no.fint.ebevis.repository.AltinnApplicationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReminderService {
    private final DataAltinnClient client;
    private final AltinnApplicationRepository repository;

    public ReminderService(DataAltinnClient client, AltinnApplicationRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public Flux<AltinnApplication> sendReminders() {
        List<AltinnApplication> applications = repository.findAllByStatus(AltinnApplicationStatus.CONSENTS_REQUESTED);

        List<AltinnApplication> reminders = applications.stream()
                .filter(application -> {
                    Duration between = Duration.between(application.getAccreditationDate(), OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));

                    return (between.toDays() > 7 && application.getAccreditationCount() < 4);
                }).collect(Collectors.toList());

        log.info("Found {} application(s) ready for reminders.", reminders.size());

        return Flux.fromIterable(reminders)
                .delayElements(Duration.ofSeconds(1))
                .flatMap(this::send)
                .onErrorContinue((a, b) -> {
                })
                .doOnNext(repository::save);
    }

    private Mono<AltinnApplication> send(AltinnApplication application) {
        return client.createReminder(application.getAccreditationId())
                .map(notifications -> {
                    notifications.stream()
                            .filter(Notification::getSuccess)
                            .findFirst()
                            .ifPresent(notification -> {
                                application.setAccreditationDate(notification.getDate());
                                application.setAccreditationCount(application.getAccreditationCount() + 1);
                            });

                    return application;
                })
                .onErrorResume(AltinnException.class, altinnException -> altinnExceptionHandler(application, altinnException));
    }

    private Mono<AltinnApplication> altinnExceptionHandler(AltinnApplication application, AltinnException altinnException) {
        log.error("Reminder of archive reference: {} - {}", application.getArchiveReference(), altinnException.getErrorCode());

        return Optional.of(altinnException)
                .map(AltinnException::getErrorCode)
                .map(ErrorCode::getCode)
                .filter(code -> code == 1019)
                .map(code -> {
                    application.setAccreditationDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));

                    return Mono.just(application);
                })
                .orElse(Mono.error(altinnException));
    }
}