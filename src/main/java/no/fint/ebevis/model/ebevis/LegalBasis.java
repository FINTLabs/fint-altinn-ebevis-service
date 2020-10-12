package no.fint.ebevis.model.ebevis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.ebevis.model.ebevis.vocab.Type;

@Data
@NoArgsConstructor
public class LegalBasis {
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private Type type;

    @JsonProperty("content")
    private String content;

    public LegalBasis(String id, Type type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }
}

