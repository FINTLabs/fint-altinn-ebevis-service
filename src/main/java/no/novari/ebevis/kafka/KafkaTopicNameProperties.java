package no.novari.ebevis.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "fint.kafka.topic")
public class KafkaTopicNameProperties {

    private String consentAcceptedPostfix;
    private String consentRequestPostfix;
    private List<String> orgs = new ArrayList<>();

    public List<String> getConsentAcceptedTopics(){
        return this.orgs.stream()
                .map(org -> org.concat(this.consentAcceptedPostfix))
                .toList();
    }

    public List<String> getConsentRequestTopics(){
        return this.orgs.stream()
                .map(org -> org.concat(this.consentRequestPostfix))
                .toList();
    }

    public String[] getConsentRequestTopicsArray() {
        return getConsentRequestTopics()
                .toArray(new String[0]);
    }
}
