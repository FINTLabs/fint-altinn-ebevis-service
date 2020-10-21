package no.fint.ebevis.model.ebevis;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Authorization {
    @JsonProperty("requestor")
    private Integer requestor;

    @JsonProperty("subject")
    private Integer subject;

    @JsonProperty("evidenceRequests")
    private List<EvidenceRequest> evidenceRequests = new ArrayList<>();

    @JsonProperty("legalBasisList")
    private List<LegalBasis> legalBasisList;

    @JsonProperty("consentReference")
    private String consentReference;

    @JsonProperty("externalReference")
    private String externalReference;

    @JsonProperty("validTo")
    private OffsetDateTime validTo;
}