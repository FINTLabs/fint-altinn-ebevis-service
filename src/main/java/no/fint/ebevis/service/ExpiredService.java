package no.fint.ebevis.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import no.fint.ebevis.repository.AltinnApplicationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpiredService {
    private final AltinnApplicationRepository repository;

    public ExpiredService(AltinnApplicationRepository repository) {
        this.repository = repository;
    }

    public Flux<AltinnApplication> setExpired() {
        List<AltinnApplication> applications = repository.findAllByStatusIn(Arrays.asList(AltinnApplicationStatus.CONSENTS_REQUESTED, AltinnApplicationStatus.CONSENTS_ACCEPTED));

        List<AltinnApplication> expired = applications.stream()
                .filter(application -> {
                    Duration between = Duration.between(application.getArchivedDate(), LocalDateTime.now());

                    return (between.toDays() >= 90);
                })
                .peek(application -> application.setStatus(AltinnApplicationStatus.CONSENTS_REJECTED_OR_EXPIRED))
                .collect(Collectors.toList());

        log.info("Found {} new application(s) with expired consent.", expired.size());

        return Flux.fromIterable(expired)
                .doOnNext(repository::save);
    }
}
