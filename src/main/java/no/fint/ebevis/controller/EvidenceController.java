package no.fint.ebevis.controller;

import lombok.extern.slf4j.Slf4j;
import no.fint.altinn.model.ebevis.ErrorCode;
import no.fint.altinn.model.ebevis.Evidence;
import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.exception.AltinnException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/evidence")
public class EvidenceController {
    private final DataAltinnClient dataAltinnClient;

    public EvidenceController(DataAltinnClient dataAltinnClient) {
        this.dataAltinnClient = dataAltinnClient;
    }

    @GetMapping("/{accreditationId}")
    public Mono<Evidence> getEvidence(@PathVariable String accreditationId, @RequestParam String evidenceCodeName) {
        return dataAltinnClient.getEvidence(accreditationId, evidenceCodeName);
    }

    @ExceptionHandler(AltinnException.class)
    public ResponseEntity<ErrorCode> handleAltinnException(AltinnException altinnException) {
        return ResponseEntity.status(altinnException.getHttpStatus()).body(altinnException.getErrorCode());
    }
}