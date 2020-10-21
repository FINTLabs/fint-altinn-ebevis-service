package no.fint.ebevis.model.ebevis;

import java.time.OffsetDateTime;
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
    private OffsetDateTime validFrom;

    @JsonProperty("validTo")
    private OffsetDateTime validTo;

    @JsonProperty("didSupplyLegalBasis")
    private Boolean didSupplyLegalBasis;
}

