package no.novari.ebevis.configuration;

import no.fint.altinn.model.ebevis.ErrorCode;
import no.novari.ebevis.exception.AltinnException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class AltinnSecurityConfiguration {
    private final AltinnProperties altinnProperties;

    public AltinnSecurityConfiguration(AltinnProperties altinnProperties) {
        this.altinnProperties = altinnProperties;
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();

        return builder
                .defaultHeader("Ocp-apim-subscription-key", altinnProperties.getOcpApimSubscriptionKey())
                .baseUrl(altinnProperties.getBaseUrl())
                .filter(ExchangeFilterFunction.ofResponseProcessor(onError))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    private final Function<ClientResponse, Mono<ClientResponse>> onError = clientResponse -> {
        if (clientResponse.statusCode().isError()) {
            return clientResponse.bodyToMono(ErrorCode.class)
                    .flatMap(errorCode -> Mono.error(new AltinnException((HttpStatus) clientResponse.statusCode(), errorCode)));
        }

        return Mono.just(clientResponse);
    };

}
