package no.novari.ebevis.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fint.altinn.model.kafka.KafkaEvidenceConsentAccepted;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsentAcceptedPublisher {
    private final KafkaTemplate<String, KafkaEvidenceConsentAccepted> kafkaTemplate;
    private final String topicName;

    public ConsentAcceptedPublisher(KafkaAdmin kafkaAdmin, KafkaTemplate<String, KafkaEvidenceConsentAccepted> kafkaTemplate, KafkaTopicNameProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topics.getConsentAccepted();

        kafkaAdmin.createOrModifyTopics(new NewTopic(topicName, 1, (short) 1));
    }

    public void publish(KafkaEvidenceConsentAccepted kafkaConsentAccepted) {

        log.info("Publishing altinn instance to topic {}: {}", topicName, kafkaConsentAccepted);

        kafkaTemplate
                .send(topicName, kafkaConsentAccepted.getAltinnReference(), kafkaConsentAccepted)
                .thenAccept(result ->
                        log.info("Published concent accepted to topic {}: {}", topicName, result))
                .exceptionally(e -> {
                    log.error("🤦 Failed to publish to topic={}", topicName, e);
                    if (e.getCause() != null) {
                        log.error("Cause: {}", e.getCause().getMessage());
                    }
                    return null;
                });
    }
}
