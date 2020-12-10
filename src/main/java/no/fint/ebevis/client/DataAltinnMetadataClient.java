package no.fint.ebevis.client;

import no.fint.altinn.model.ebevis.ErrorCode;
import no.fint.altinn.model.ebevis.EvidenceCode;
import no.fint.altinn.model.ebevis.EvidenceStatusCode;
import no.fint.ebevis.configuration.AltinnProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class DataAltinnMetadataClient {
    private final WebClient webClient;

    public DataAltinnMetadataClient(WebClient.Builder builder, final AltinnProperties altinnProperties) {
        this.webClient = builder
                .baseUrl(altinnProperties.getBaseUrl())
                .build();
    }

    public Mono<List<ErrorCode>> getErrorCodes() {
        return webClient.get()
                .uri("/public/metadata/errorcodes")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ErrorCode>>() {});
    }

    public Mono<List<EvidenceStatusCode>> getStatusCodes() {
        return webClient.get()
                .uri("/public/metadata/statuscodes")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceStatusCode>>() {});
    }

    public Mono<List<EvidenceCode>> getEvidenceCodes() {
        return webClient.get()
                .uri("/public/metadata/evidencecodes")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceCode>>() {});
    }

    public Mono<List<String>> getServiceContexts() {
        return webClient.get()
                .uri("/public/metadata/servicecontexts")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {});
    }

    public Mono<List<EvidenceCode>> getEvidenceCodesWithinServiceContext(String serviceContext) {
        return webClient.get()
                .uri("/public/metadata/evidencecodes/{serviceContext}", serviceContext)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceCode>>() {});
    }
}
