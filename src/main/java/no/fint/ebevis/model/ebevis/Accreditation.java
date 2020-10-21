package no.fint.ebevis.model.ebevis;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Accreditation {
    @JsonProperty("id")
    private String id;

    @JsonProperty("requestor")
    private String requestor;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("aggregateStatus")
    private EvidenceStatusCode aggregateStatus;

    @JsonProperty("evidenceCodes")
    private List<EvidenceCode> evidenceCodes;

    @JsonProperty("issued")
    private OffsetDateTime issued;

    @JsonProperty("lastChanged")
    private OffsetDateTime lastChanged;

    @JsonProperty("validTo")
    private OffsetDateTime validTo;

    @JsonProperty("consentReference")
    private String consentReference;

    @JsonProperty("externalReference")
    private String externalReference;

    @JsonProperty("languageCode")
    private String languageCode;
}

