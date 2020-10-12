package no.fint.ebevis.model.ebevis.vocab;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ParamType {
    BOOLEAN("boolean"),
    STRING("string"),
    NUMBER("number"),
    DATETIME("dateTime"),
    ATTACHMENT("attachment"),
    URI("uri");

    private final String value;

    ParamType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
