package no.novari.ebevis.client;

import no.fint.altinn.model.ebevis.*;
import no.novari.ebevis.maskinporten.MaskinportenService;
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
    private final MaskinportenService maskinporten;

    public DataAltinnClient(WebClient webClient, MaskinportenService maskinporten) {
        this.webClient = webClient;
        this.maskinporten = maskinporten;
    }

    public Mono<Accreditation> createAccreditation(Authorization authorization) {
        return maskinporten.getBearerToken().flatMap(bearerToken
                -> webClient.post()
                .uri("/authorization")
                .header("Authorization", bearerToken
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authorization)
                .retrieve()
                .bodyToMono(Accreditation.class));
    }

    public Mono<List<Notification>> createReminder(String accreditationId) {
        return maskinporten.getBearerToken().flatMap(bearerToken
                -> webClient.post()
                .uri("/accreditations/{accreditationId}/reminders", accreditationId)
                .header("Authorization", bearerToken
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Notification>>() {}));
    }

    public Mono<ResponseEntity<Void>> deleteAccreditation(String accreditationId) {
        return maskinporten.getBearerToken().flatMap(bearerToken
                -> webClient.delete()
                .uri("/accreditations/{accreditationId}", accreditationId)
                .header("Authorization", bearerToken
                )
                .retrieve()
                .toBodilessEntity());
    }

    public Mono<List<Accreditation>> getAccreditations(OffsetDateTime changedAfter) {
        return maskinporten.getBearerToken().flatMap(bearerToken
                -> webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/accreditations")
                        .queryParam("changedafter", changedAfter)
                        .build())
                .header("Authorization", bearerToken
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Accreditation>>() {}));
    }

    public Mono<Evidence> getEvidence(String accreditationId, String evidenceCode) {
        return maskinporten.getBearerToken().flatMap(bearerToken -> webClient.get()
                    .uri("/evidence/{accreditationId}/{evidenceCode}", accreditationId, evidenceCode)
                    .header("Authorization", bearerToken)
                    .retrieve()
                    .bodyToMono(Evidence.class));
    }

    public Mono<List<EvidenceStatus>> getEvidenceStatuses(String id) {
        return maskinporten.getBearerToken().flatMap(bearerToken
                -> webClient.get()
                .uri("/evidence/{id}", id)
                .header("Authorization", bearerToken
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceStatus>>() {}));
    }

}
