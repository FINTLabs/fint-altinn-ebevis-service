package no.fint.ebevis.model.ebevis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EvidenceRequest {
    @JsonProperty("evidenceCodeName")
    private String evidenceCodeName;

    @JsonProperty("legalBasisId")
    private String legalBasisId;

    @JsonProperty("legalBasisReference")
    private String legalBasisReference;

    @JsonProperty("requestConsent")
    private Boolean requestConsent;

    @JsonProperty("parameters")
    private List<EvidenceParameter> parameters;
}

