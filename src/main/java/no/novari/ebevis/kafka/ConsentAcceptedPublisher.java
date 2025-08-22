package no.novari.ebevis.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import no.fint.altinn.model.kafka.KafkaEvidenceConsentAccepted;
import no.novari.ebevis.repository.AltinnApplicationRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class ConsentAcceptedPublisher {
    private final KafkaTemplate<String, KafkaEvidenceConsentAccepted> kafkaTemplate;
    private final String topicName;
    private final AltinnApplicationRepository repository;

    public ConsentAcceptedPublisher(KafkaTemplate<String, KafkaEvidenceConsentAccepted> kafkaTemplate, KafkaTopicNameProperties topics, AltinnApplicationRepository repository) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topics.getConsentAccepted();
        this.repository = repository;
    }

    public void publish(KafkaEvidenceConsentAccepted kafkaConsentAccepted) {
        String key = kafkaConsentAccepted.getAltinnReference();
        Assert.hasText(key, "altinnReference must not be null or blank");

        log.info("Publishing consent accepted to topic={}, key={}", topicName, key);

        kafkaTemplate
                .send(topicName, key, kafkaConsentAccepted)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish to topic={}, key={}", topicName, key, ex);
                    } else if (result != null && result.getRecordMetadata() != null) {
                        var meta = result.getRecordMetadata();
                        log.info("Published consent accepted to topic={}, partition={}, offset={}, key={}", meta.topic(), meta.partition(), meta.offset(), key);
                    } else {
                        log.info("Published consent accepted to topic={}, key={}, (no metadata)", topicName, key);
                    }
                });
    }

    public Flux<AltinnApplication> sendAcceptedConsents() {
        return Flux.fromIterable(repository.findAllByStatus(AltinnApplicationStatus.CONSENTS_ACCEPTED))
                .doOnNext(this::publishAcceptedConsent)
                .map(this::setApplicatonStatus)
                .onErrorContinue((ex, result) -> log.debug("Error when publishing consent accepted: {}, {}", ex, result))
                .map(repository::save);
    }

    private void publishAcceptedConsent(AltinnApplication application) {
        KafkaEvidenceConsentAccepted consentAccepted = KafkaEvidenceConsentAccepted.builder()
                .altinnReference(application.getArchiveReference())
                .organizationNumber(application.getRequestor())
                .countyOrganizationNumber(application.getRequestor())
                .fintOrgId(application.getFintOrgId())
                .consentsAccepted(List.of("RestanserV2", "KonkursDrosje"))
                .build();

        this.publish(consentAccepted);
    }

    private AltinnApplication setApplicatonStatus(AltinnApplication application) {
        application.setStatus(AltinnApplicationStatus.PURGED);
        return application;
    }
}
