package no.fint.ebevis.model.ebevis;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Evidence {
    @JsonProperty("name")
    private String name;

    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;

    @JsonProperty("evidenceStatus")
    private EvidenceStatus evidenceStatus;

    @JsonProperty("evidenceValues")
    private List<EvidenceValue> evidenceValues;
}

