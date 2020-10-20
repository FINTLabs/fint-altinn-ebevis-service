package no.fint.ebevis.configuration;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;

@Configuration
public class SecurityConfiguration {
    private final AltinnProperties altinnProperties;

    public SecurityConfiguration(AltinnProperties altinnProperties) {
        this.altinnProperties = altinnProperties;
    }

    @Bean
    public ClientHttpConnector clientHttpConnector(SslContext sslContext) {
        return new ReactorClientHttpConnector(HttpClient
                .create(ConnectionProvider.newConnection())
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext)));
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder, ClientHttpConnector clientHttpConnector) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();

        return builder
                .defaultHeader("Ocp-apim-subscription-key", altinnProperties.getOcpApimSubscriptionKey())
                .baseUrl(altinnProperties.getBaseUrl())
                .clientConnector(clientHttpConnector)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    @Bean
    public SslContext sslContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(getClass().getClassLoader().getResourceAsStream(altinnProperties.getKeyStoreFile()), altinnProperties.getKeyStorePassword().toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, altinnProperties.getKeyStorePassword().toCharArray());

            return SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error creating SSL context");
        }
    }
}
