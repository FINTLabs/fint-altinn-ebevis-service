package no.fint.ebevis.client;

import no.fint.ebevis.configuration.AltinnConfiguration;
import no.fint.ebevis.model.ebevis.*;
import no.fint.ebevis.model.ebevis.Error;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

@Component
public class EBevisClient {
    private final WebClient webClient;

    public EBevisClient(WebClient.Builder webClientBuilder, AltinnConfiguration altinnConfiguration) {
        this.webClient = webClientBuilder
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.add("Ocp-Apim-Subscription-Key", altinnConfiguration.getOcpApimSubscriptionKey());
                    httpHeaders.add("X-NADOBE-CERT", "TODO");
                })
                .baseUrl(altinnConfiguration.getBaseUrl())
                .build();
    }

    public Mono<ResponseEntity<Accreditation>> createAccreditation(Authorization authorization) {
        return webClient.post()
                .uri("/authorization")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authorization)
                .retrieve()
                .toEntity(Accreditation.class);
    }

    public Mono<ResponseEntity<Void>> deleteAccreditation(String accreditationId) {
        return webClient.delete()
                .uri("/accreditations/{accreditationId}", accreditationId)
                .retrieve()
                .toBodilessEntity();
    }

    /*
    query parameters - might only be available in staging/test environment
     */
    public Flux<Accreditation> getAccreditations(String requestor, ZonedDateTime changedAfter, Boolean onlyAvailable) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/accreditations")
                        .queryParam("requestor", requestor)
                        .queryParam("changedafter", changedAfter)
                        .queryParam("onlyavailable", onlyAvailable)
                        .build())
                .retrieve()
                .bodyToFlux(Accreditation.class);
    }

    public Mono<Evidence> getEvidence(String accreditationId, String evidenceCode) {
        return webClient.get()
                .uri("/evidence/{accreditationId}/{evidenceCode}", accreditationId, evidenceCode)
                .retrieve()
                .bodyToMono(Evidence.class);
    }

    public Flux<EvidenceStatus> getEvidenceStatuses(String accreditationId) {
        return webClient.get()
                .uri("/evidence/{accreditationId}", accreditationId)
                .retrieve()
                .bodyToFlux(EvidenceStatus.class);
    }

    public Flux<Error> getErrorCodes() {
        return webClient.get()
                .uri("/public/metadata/errorcodes")
                .retrieve()
                .bodyToFlux(Error.class);
    }

    public Flux<EvidenceCode> getEvidenceCodes() {
        return webClient.get()
                .uri("/public/metadata/evidencecodes")
                .retrieve()
                .bodyToFlux(EvidenceCode.class);
    }

    public Flux<EvidenceStatusCode> getStatusCodes() {
        return webClient.get()
                .uri("/public/metadata/statuscodes")
                .retrieve()
                .bodyToFlux(EvidenceStatusCode.class);
    }
}
