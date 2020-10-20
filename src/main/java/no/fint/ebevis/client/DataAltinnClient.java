package no.fint.ebevis.client;

import no.fint.ebevis.model.ebevis.*;
import no.fint.ebevis.model.ebevis.Error;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class DataAltinnClient {
    private final WebClient webClient;

    public DataAltinnClient(WebClient webClient) {
        this.webClient = webClient;
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

    public Mono<List<Accreditation>> getAccreditations(String requestor, ZonedDateTime changedAfter, Boolean onlyAvailable) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/accreditations")
                        .queryParam("requestor", requestor)
                        .queryParam("changedafter", changedAfter)
                        .queryParam("onlyavailable", onlyAvailable)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Accreditation>>() {
                });
    }

    public Mono<Evidence> getEvidence(String accreditationId, String evidenceCode) {
        return webClient.get()
                .uri("/evidence/{accreditationId}/{evidenceCode}", accreditationId, evidenceCode)
                .retrieve()
                .bodyToMono(Evidence.class);
    }

    public Mono<List<EvidenceStatus>> getEvidenceStatuses(String accreditationId) {
        return webClient.get()
                .uri("/evidence/{accreditationId}", accreditationId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceStatus>>() {
                });
    }

    public Mono<List<Error>> getErrorCodes() {
        return webClient.get()
                .uri("/public/metadata/errorcodes")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Error>>() {
                });
    }

    public Mono<List<EvidenceCode>> getEvidenceCodes() {
        return webClient.get()
                .uri("/public/metadata/evidencecodes")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceCode>>() {
                });
    }

    public Mono<List<EvidenceStatusCode>> getStatusCodes() {
        return webClient.get()
                .uri("/public/metadata/statuscodes")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceStatusCode>>() {
                });
    }
}
