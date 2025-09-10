package no.novari.ebevis.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.ebevis.ErrorCode;
import no.fint.altinn.model.ebevis.Evidence;
import no.fint.altinn.model.ebevis.EvidenceStatus;
import no.novari.ebevis.client.DataAltinnClient;
import no.novari.ebevis.exception.AltinnException;
import no.novari.ebevis.repository.AltinnApplicationRepository;
import no.novari.ebevis.util.CertificateConverter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/evidence")
public class EvidenceController {
    private final DataAltinnClient dataAltinnClient;
    private final CertificateConverter certificateConverter;
    private final AltinnApplicationRepository altinnApplicationRepository;

    public EvidenceController(DataAltinnClient dataAltinnClient, CertificateConverter certificateConverter, AltinnApplicationRepository altinnApplicationRepository) {
        this.dataAltinnClient = dataAltinnClient;
        this.certificateConverter = certificateConverter;
        this.altinnApplicationRepository = altinnApplicationRepository;
    }

    @GetMapping("/{accreditationId}")
    public Mono<Evidence> getEvidence(@PathVariable String accreditationId, @RequestParam String evidenceCodeName) {
        return dataAltinnClient.getEvidence(accreditationId, evidenceCodeName);
    }

    @GetMapping("/status/{accreditationId}")
    public Mono<List<EvidenceStatus>> getEvidenceStatus(@PathVariable String accreditationId) {
        return dataAltinnClient.getEvidenceStatuses(accreditationId);
    }

    @GetMapping("/file/{partyId}/{instanceId}/{evidenceCodeName}")
    public Mono<ResponseEntity<ByteArrayResource>> getEvidenceCertificate(@PathVariable String partyId,
                                                                          @PathVariable String instanceId,
                                                                          @PathVariable String evidenceCodeName) {
        String partyInstanceId = partyId.concat("/").concat(instanceId);
        AltinnApplication altinnaApplication = altinnApplicationRepository.findAllByInstanceId(partyInstanceId).getFirst();

        return dataAltinnClient.getEvidence(altinnaApplication.getAccreditationId(), evidenceCodeName)
                            .map(evidence -> {
                                byte[] certificate = certificateConverter.convertCertificate(evidence, altinnaApplication, evidenceCodeName);

                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.APPLICATION_PDF);
                                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=evidence_" + altinnaApplication.getAccreditationId() + ".pdf");

                                return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(new ByteArrayResource(certificate));
                            });
    }

    @ExceptionHandler(AltinnException.class)
    public ResponseEntity<ErrorCode> handleAltinnException(AltinnException altinnException) {
        return ResponseEntity.status(altinnException.getHttpStatus()).body(altinnException.getErrorCode());
    }
}