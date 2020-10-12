package no.fint.ebevis.model.ebevis;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Accreditation {
    @JsonProperty("accreditationId")
    private String accreditationId;

    @JsonProperty("requestor")
    private String requestor;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("aggregateStatus")
    private EvidenceStatusCode aggregateStatus;

    @JsonProperty("evidenceCodes")
    private List<EvidenceCode> evidenceCodes;

    @JsonProperty("issued")
    private ZonedDateTime issued;

    @JsonProperty("lastChanged")
    private ZonedDateTime lastChanged;

    @JsonProperty("validTo")
    private ZonedDateTime validTo;

    @JsonProperty("tedReference")
    private String tedReference;

    @JsonProperty("doffinReference")
    private String doffinReference;

    @JsonProperty("externalReference")
    private String externalReference;

    public Accreditation(String accreditationId, String subject, ZonedDateTime issued, ZonedDateTime validTo) {
        this.accreditationId = accreditationId;
        this.subject = subject;
        this.issued = issued;
        this.validTo = validTo;
    }
}

