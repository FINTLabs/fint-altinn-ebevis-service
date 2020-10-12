package no.fint.ebevis.model.ebevis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Error {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("description")
    private String description;

    public Error(Integer code) {
        this.code = code;
    }
}

