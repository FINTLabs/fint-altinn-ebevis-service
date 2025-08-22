package no.novari.ebevis.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class KafkaTopicConfig {

    private final KafkaTopicNameProperties topics;

    public KafkaTopicConfig(KafkaTopicNameProperties topics) {
        this.topics = topics;
    }

    @Bean
    public NewTopic consentAcceptedTopic() {
        return new NewTopic(topics.getConsentAccepted(), 1, (short) 1);
    }

    @Bean
    public NewTopic consentRequestTopic() {
        return new NewTopic(topics.getConsentRequest(), 1, (short) 1);
    }
}