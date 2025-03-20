package no.novari.ebevis.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("altinn")
public class AltinnProperties {
    private String baseUrl;
    private String ocpApimSubscriptionKey;
}
