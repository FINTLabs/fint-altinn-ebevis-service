package no.novari.ebevis.exception;

import lombok.Getter;
import no.fint.altinn.model.ebevis.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public class AltinnException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public AltinnException(HttpStatus httpStatus, ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
