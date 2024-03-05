package no.fint.ebevis.configuration;

import no.fint.altinn.model.ebevis.ErrorCode;
import no.fint.ebevis.exception.AltinnException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class AltinnSecurityConfiguration {
    private final AltinnProperties altinnProperties;
    private final MaskinportenConfiguration maskinportenConfiguration;

    public AltinnSecurityConfiguration(AltinnProperties altinnProperties, MaskinportenConfiguration maskinportenConfiguration) {
        this.altinnProperties = altinnProperties;
        this.maskinportenConfiguration = maskinportenConfiguration;
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder, ClientHttpConnector clientHttpConnector) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();

        String token = maskinportenConfiguration.makeTokenRequest();

        return builder
                .defaultHeader("Ocp-apim-subscription-key", altinnProperties.getOcpApimSubscriptionKey())
                .defaultHeader("Authorization", "Bearer " + token)
                .baseUrl(altinnProperties.getBaseUrl())
                .filter(ExchangeFilterFunction.ofResponseProcessor(onError))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    private final Function<ClientResponse, Mono<ClientResponse>> onError = clientResponse -> {
        if (clientResponse.statusCode().isError()) {
            return clientResponse.bodyToMono(ErrorCode.class)
                    .flatMap(errorCode -> Mono.error(new AltinnException(clientResponse.statusCode(), errorCode)));
        }

        return Mono.just(clientResponse);
    };

}
