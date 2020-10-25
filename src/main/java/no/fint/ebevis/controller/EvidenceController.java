package no.fint.ebevis.controller;

import no.fint.ebevis.client.DataAltinnClient;
import no.fint.ebevis.model.ebevis.Evidence;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

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

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(WebClientResponseException.class)
    public void notFound() {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public void badRequest() {
    }
}