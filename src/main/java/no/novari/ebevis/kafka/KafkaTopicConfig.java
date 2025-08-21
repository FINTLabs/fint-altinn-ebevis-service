package no.novari.ebevis.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class KafkaTopicConfig {

    private final KafkaTopicNameProperties topics;

    public KafkaTopicConfig(KafkaTopicNameProperties topics) {
        this.topics = topics;
    }
}