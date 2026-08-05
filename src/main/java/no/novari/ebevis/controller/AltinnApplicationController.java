package no.novari.ebevis.controller;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.altinn.model.AltinnApplication;
import no.novari.fint.altinn.model.AltinnApplicationStatus;
import no.novari.ebevis.repository.AltinnApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/applications")
public class AltinnApplicationController {
    private final AltinnApplicationRepository repository;

    public AltinnApplicationController(AltinnApplicationRepository repository) {
        this.repository = repository;
    }

    @PatchMapping("/{archiveReference}/status")
    public ResponseEntity<AltinnApplication> updateStatus(@PathVariable String archiveReference,
                                                            @RequestBody AltinnApplicationStatus status) {
        return repository.findById(archiveReference)
                .map(application -> {
                    log.info("Updating status of archive reference: {} from {} to {}", archiveReference, application.getStatus(), status);

                    application.setStatus(status);

                    return ResponseEntity.ok(repository.save(application));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
