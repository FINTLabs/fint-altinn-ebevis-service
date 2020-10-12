package no.fint.ebevis.model.ebevis;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Evidence {
    @JsonProperty("name")
    private String name;

    @JsonSetter("timestamp")
    private ZonedDateTime timestamp;

    @JsonProperty("evidenceStatus")
    private EvidenceStatus evidenceStatus;

    @JsonProperty("evidenceValues")
    private List<EvidenceValue> evidenceValues;

    public Evidence(String name, ZonedDateTime timestamp, EvidenceStatus evidenceStatus, List<EvidenceValue> evidenceValues) {
        this.name = name;
        this.timestamp = timestamp;
        this.evidenceStatus = evidenceStatus;
        this.evidenceValues = evidenceValues;
    }
}

