package no.fint.ebevis.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("maskinporten")
public class MaskinportenProperties {
    private String tokenEndpoint;
    private String issuer;
    private String audience;
    private String scope;
    private String kid;
    private String privateKey;
}