package no.novari.ebevis.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class KafkaTopicConfig {

    private final KafkaTopicNameProperties topics;

    public KafkaTopicConfig(KafkaTopicNameProperties topics) {
        this.topics = topics;
    }

    @Bean
    public List<NewTopic> consentAcceptedTopic() {
        log.info("Creating topic(s) for consent accepted: {}", topics.getConsentAcceptedTopics());
        return topics.getConsentAcceptedTopics().stream()
                .map(topicName -> new NewTopic(topicName, 1, (short) 1))
                .collect(Collectors.toList());
    }

    @Bean
    public List<NewTopic> consentRequestTopics() {
        log.info("Creating topic(s) for consent requests: {}", topics.getConsentRequestTopics());
        return topics.getConsentRequestTopics().stream()
                .map(topicName -> new NewTopic(topicName, 1, (short) 1))
                .collect(Collectors.toList());
    }
}