package no.fint.ebevis.model.ebevis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.ebevis.model.ebevis.vocab.ParamType;

@Data
@NoArgsConstructor
public class EvidenceParameter {
    @JsonProperty("evidenceParamName")
    private String evidenceParamName;

    @JsonProperty("paramType")
    private ParamType paramType;

    @JsonProperty("required")
    private Boolean required;

    @JsonProperty("value")
    private Object value;

    public EvidenceParameter(String evidenceParamName) {
        this.evidenceParamName = evidenceParamName;
    }
}

