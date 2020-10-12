package no.fint.ebevis.model.ebevis;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.ebevis.model.ebevis.vocab.ValueType;
import org.springframework.http.MediaType;

@Data
@NoArgsConstructor
public class EvidenceValue {
    @JsonProperty("evidenceValueName")
    private String evidenceValueName;

    @JsonProperty("valueType")
    private ValueType valueType;

    @JsonProperty("mimeType")
    private MediaType mimeType;

    @JsonProperty("source")
    private String source;

    @JsonProperty("value")
    private Object value;

    @JsonProperty("timestamp")
    private ZonedDateTime timestamp;

    public EvidenceValue(String evidenceValueName, ValueType valueType, String source) {
        this.evidenceValueName = evidenceValueName;
        this.valueType = valueType;
        this.source = source;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = MediaType.parseMediaType(mimeType);
    }
}

