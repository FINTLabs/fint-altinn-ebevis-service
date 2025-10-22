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

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class ConsentAcceptedProducer {
    private final KafkaTemplate<String, KafkaEvidenceConsentAccepted> kafkaTemplate;
    private final String topicNamePostfix;
    private final AltinnApplicationRepository repository;

    public ConsentAcceptedProducer(KafkaTemplate<String, KafkaEvidenceConsentAccepted> kafkaTemplate, KafkaTopicNameProperties topics, AltinnApplicationRepository repository) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicNamePostfix = topics.getConsentAcceptedPostfix();
        this.repository = repository;
    }

    public void publish(KafkaEvidenceConsentAccepted kafkaConsentAccepted) {
        String key = kafkaConsentAccepted.getAltinnInstanceId();
        Assert.hasText(key, "altinnReference must not be null or blank");

        String topicName = kafkaConsentAccepted.getFintOrgId()
                .replace(".", "-")
                .concat(topicNamePostfix);

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
                .filter(application -> {
                    boolean isDrosjesentral = !application.getArchiveReference().startsWith("AR");
                    if (!isDrosjesentral) {
                        log.info("Application {} is a drosjeløyvesøknad and will not be sent to FLYT.", application.getArchiveReference());
                    }
                    return isDrosjesentral;
                })
                .map(this::publishAcceptedConsent)
                .map(this::setApplicatonStatus)
                .delayElements(Duration.ofMillis(1000))
                .onErrorContinue((ex, result) -> log.debug("Error when publishing consent accepted: {}, {}", ex, result))
                .map(repository::save);
    }

    private AltinnApplication publishAcceptedConsent(AltinnApplication application) {

        KafkaEvidenceConsentAccepted consentAccepted = KafkaEvidenceConsentAccepted.builder()
                .altinnInstanceId(application.getInstanceId())
                .organizationNumber(application.getSubject())
                .organizationName(application.getSubjectName())
                .countyOrganizationNumber(application.getRequestor())
                .fintOrgId(application.getFintOrgId())
                .consentsAccepted(List.of("RestanserV2", "KonkursDrosje"))
                .build();

        this.publish(consentAccepted);

        return application;
    }

    private AltinnApplication setApplicatonStatus(AltinnApplication application) {
        application.setStatus(AltinnApplicationStatus.PURGED);
        return application;
    }
}
