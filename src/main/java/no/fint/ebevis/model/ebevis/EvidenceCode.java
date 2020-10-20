package no.fint.ebevis.model.ebevis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

import lombok.NoArgsConstructor;
import no.fint.ebevis.model.ebevis.vocab.AccessMethod;

@Data
@NoArgsConstructor
public class EvidenceCode {
    @JsonProperty("evidenceCodeName")
    private String evidenceCodeName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("accessMethod")
    private AccessMethod accessMethod;

    @JsonProperty("parameters")
    private List<EvidenceParameter> parameters;

    @JsonProperty("isAsynchronous")
    private Boolean isAsynchronous;

    @JsonProperty("maxValidDays")
    private Integer maxValidDays;

    @JsonProperty("values")
    private List<EvidenceValue> values;

    @JsonProperty("serviceContext")
    private String serviceContext;

    public EvidenceCode(String evidenceCodeName, AccessMethod accessMethod, Boolean isAsynchronous, List<EvidenceValue> values) {
        this.evidenceCodeName = evidenceCodeName;
        this.accessMethod = accessMethod;
        this.isAsynchronous = isAsynchronous;
        this.values = values;
    }
}

