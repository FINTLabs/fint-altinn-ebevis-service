package no.fint.ebevis.model.ebevis.vocab;

import com.fasterxml.jackson.annotation.JsonValue;

public enum  NotificationType {
    SMS("SMS"),
    EMAIL("Email");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
