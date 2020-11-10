package no.fint.ebevis.client;

import no.fint.ebevis.model.ebevis.*;
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

    public Mono<ResponseEntity<Notification>> createReminders(String accreditationId) {
        return webClient.post()
                .uri("/accreditations/{accreditationId}/reminders", accreditationId)
                .retrieve()
                .toEntity(Notification.class);
    }

    public Mono<List<Notification>> createReminder(String accreditationId) {
        return webClient.post()
                .uri("/accreditations/{accreditationId}/reminders", accreditationId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Notification>>() {});
    }

    public Mono<ResponseEntity<Void>> deleteAccreditation(String accreditationId) {
        return webClient.delete()
                .uri("/accreditations/{accreditationId}", accreditationId)
                .retrieve()
                .toBodilessEntity();
    }

    public Mono<List<Accreditation>> getAccreditations(OffsetDateTime changedAfter) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/accreditations")
                        .queryParam("changedafter", changedAfter)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Accreditation>>() {});
    }

    public Mono<Evidence> getEvidence(String accreditationId, String evidenceCode) {
        return webClient.get()
                .uri("/evidence/{accreditationId}/{evidenceCode}", accreditationId, evidenceCode)
                .retrieve()
                .bodyToMono(Evidence.class);
    }

    public Mono<List<EvidenceStatus>> getEvidenceStatuses(String id) {
        return webClient.get()
                .uri("/evidence/{id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EvidenceStatus>>() {});
    }
}
