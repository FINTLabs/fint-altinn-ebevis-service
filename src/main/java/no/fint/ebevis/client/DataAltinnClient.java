package no.fint.ebevis.client;

import no.fint.altinn.model.ebevis.*;
import no.fint.ebevis.configuration.MaskinportenConfiguration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class DataAltinnClient {
    private final WebClient webClient;
    private final MaskinportenConfiguration maskinporten;

    public DataAltinnClient(WebClient webClient, MaskinportenConfiguration maskinporten) {
        this.webClient = webClient;
        this.maskinporten = maskinporten;
    }

    public Mono<Accreditation> createAccreditation(Authorization authorization) {
        return maskinporten.getAccessToken().flatMap(accessToken -> webClient.post()
                .uri("/authorization")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authorization)
                .retrieve()
                .bodyToMono(Accreditation.class));
    }

    public Mono<List<Notification>> createReminder(String accreditationId) {
        return maskinporten.getAccessToken().flatMap(accessToken -> webClient.post()
                .uri("/accreditations/{accreditationId}/reminders", accreditationId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Notification>>() {}));
    }

    public Mono<ResponseEntity<Void>> deleteAccreditation(String accreditationId) {
        return maskinporten.getAccessToken().flatMap(accessToken -> webClient.delete()
                .uri("/accreditations/{accreditationId}", accreditationId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity());
    }

    public Mono<List<Accreditation>> getAccreditations(OffsetDateTime changedAfter) {
        return maskinporten.getAccessToken().flatMap(accessToken -> webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/accreditations")
                        .queryParam("changedafter", changedAfter)
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Accreditation>>() {}));
    }

    public Mono<Evidence> getEvidence(String accreditationId, String evidenceCode) {
        return maskinporten.getAccessToken().flatMap(accessToken -> webClient.get()
                    .uri("/evidence/{accreditationId}/{evidenceCode}", accreditationId, evidenceCode)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Evidence.class));
    }

    public Mono<List<EvidenceStatus>> getEvidenceStatuses(String id) {
        return maskinporten.getAccessToken().flatMap(accessToken -> webClient.get()
                .uri("/evidence/{id}", id)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceStatus>>() {}));
    }

}
