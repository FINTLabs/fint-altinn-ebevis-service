package no.novari.ebevis.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.novari.fint.altinn.model.ebevis.*;
import no.novari.ebevis.maskinporten.MaskinportenService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
public class DataAltinnClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        log.info("Fetching evidence from Altinn, accreditationId={}, evidenceCode={}", accreditationId, evidenceCode);

        return maskinporten.getBearerToken().flatMap(bearerToken -> webClient.get()
                        .uri("/evidence/{accreditationId}/{evidenceCode}", accreditationId, evidenceCode)
                        .header("Authorization", bearerToken)
                        .retrieve()
                        .bodyToMono(Evidence.class))
                .doOnSuccess(evidence -> {
                    if (evidence == null) {
                        log.warn("Altinn evidence response was null, accreditationId={}, evidenceCode={}", accreditationId, evidenceCode);
                        return;
                    }

                    int evidenceValueCount = evidence.getEvidenceValues() == null ? 0 : evidence.getEvidenceValues().size();
                    String statusCodeName = evidence.getEvidenceStatus() == null ? null : evidence.getEvidenceStatus().getEvidenceCodeName();

                    log.info("Received Altinn evidence, accreditationId={}, evidenceCode={}, evidenceStatusCodeName={}, evidenceValueCount={}",
                            accreditationId,
                            evidenceCode,
                            statusCodeName,
                            evidenceValueCount);

                    if ("RestanserV2".equals(evidenceCode)) {
                        try {
                            String evidenceAsJson = OBJECT_MAPPER.writeValueAsString(evidence);
                            log.debug("RestanserV2 JSON: {}", evidenceAsJson);
                        } catch (JsonProcessingException e) {
                            log.warn("Unable to serialize Altinn evidence response to JSON, accreditationId={}, evidenceCode={}",
                                    accreditationId,
                                    evidenceCode,
                                    e);
                        }
                    }

                    log.debug("Raw Altinn evidence response for accreditationId={}, evidenceCode={}: {}",
                            accreditationId,
                            evidenceCode,
                            evidence);
                })
                .doOnError(error -> log.error("Failed to fetch evidence from Altinn, accreditationId={}, evidenceCode={}",
                        accreditationId,
                        evidenceCode,
                        error));
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
