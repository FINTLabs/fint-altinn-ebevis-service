package no.fint.ebevis.model.ebevis.vocab;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AccessMethod {
    OPEN("open"),
    CONSENT("consent"),
    LEGALBASIS("legalBasis"),
    CONSENTORLEGALBASIS("consentOrLegalBasis");

    private final String value;

    AccessMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
