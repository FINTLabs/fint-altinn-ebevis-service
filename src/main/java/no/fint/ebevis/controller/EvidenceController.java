package no.fint.ebevis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.model.ebevis.ErrorCode;
import no.fint.ebevis.model.ebevis.Evidence;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/evidence")
public class EvidenceController {
    private final DataAltinnClient dataAltinnClient;
    private final ObjectMapper objectMapper;

    public EvidenceController(DataAltinnClient dataAltinnClient, ObjectMapper objectMapper) {
        this.dataAltinnClient = dataAltinnClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{accreditationId}")
    public Mono<Evidence> getEvidence(@PathVariable String accreditationId, @RequestParam String evidenceCodeName) {
        return dataAltinnClient.getEvidence(accreditationId, evidenceCodeName);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorCode> webClientResponseException(WebClientResponseException webClientResponseException) {
        ErrorCode errorCode;

        try {
            errorCode = objectMapper.readValue(webClientResponseException.getResponseBodyAsString(), ErrorCode.class);
        } catch (JsonProcessingException ex) {
            errorCode = new ErrorCode();
        }

        return ResponseEntity.status(webClientResponseException.getStatusCode()).body(errorCode);
    }
}