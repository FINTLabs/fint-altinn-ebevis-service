package no.fint.ebevis.model.ebevis;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EvidenceStatus {
    @JsonProperty("evidenceCodeName")
    private String evidenceCodeName;

    @JsonProperty("status")
    private EvidenceStatusCode status;

    @JsonProperty("validFrom")
    private ZonedDateTime validFrom;

    @JsonProperty("validTo")
    private ZonedDateTime validTo;

    @JsonProperty("didSupplyLegalBasis")
    private Boolean didSupplyLegalBasis;

    public EvidenceStatus(String evidenceCodeName, EvidenceStatusCode status, ZonedDateTime validFrom, ZonedDateTime validTo, Boolean didSupplyLegalBasis) {
        this.evidenceCodeName = evidenceCodeName;
        this.status = status;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.didSupplyLegalBasis = didSupplyLegalBasis;
    }
}

