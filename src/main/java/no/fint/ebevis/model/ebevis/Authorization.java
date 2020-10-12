package no.fint.ebevis.model.ebevis;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Authorization {
    @JsonProperty("requestor")
    private String requestor;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("evidenceRequests")
    private List<EvidenceRequest> evidenceRequests;

    @JsonProperty("legalBasisList")
    private List<LegalBasis> legalBasisList;

    @JsonProperty("tedReference")
    private String tedReference;

    @JsonProperty("doffinReference")
    private String doffinReference;

    @JsonProperty("externalReference")
    private String externalReference;

    @JsonProperty("validTo")
    private ZonedDateTime validTo;
}

