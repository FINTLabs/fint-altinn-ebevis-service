package no.fint.ebevis.model.ebevis.vocab;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Type {
    ESPD("ESPD");

    private final String value;

    Type(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
