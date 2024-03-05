package no.fint.ebevis.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenResponse {
    private String access_token;
    private String token_type;
    private int expires_in;
    private String scope;
}