package no.fint.ebevis.configuration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;

@Configuration
@Slf4j
public class MaskinportenConfiguration {

    public static final long JWT_EXPIRATION_OFFSET = 5000;
    public static final long JWT_EXPIRATION_TIME = 120000;
    private final MaskinportenProperties maskinportenProperties;
    private long tokenExpirationTime = 0;
    private String bearerToken;

    public MaskinportenConfiguration(MaskinportenProperties maskinportenProperties) {
        this.maskinportenProperties = maskinportenProperties;
    }

    public Mono<String> getBearerToken() {
        try {
            if (!StringUtils.isEmpty(bearerToken) && !isJwtExpired()){
                log.debug("🔑 Use existing token.");
                return Mono.just(bearerToken);
            }
            log.debug("🔑 Create new token.");

            return WebClient.builder().baseUrl(maskinportenProperties.getTokenEndpoint()).build()
                    .post()
                    .body(BodyInserters
                            .fromFormData("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                            .with("assertion", createSignedJwt()))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .onErrorMap(e -> new RuntimeException("Error fetching token", e))
                    .flatMap(response -> {
                        bearerToken = "Bearer " + response.getAccessToken();
                        return Mono.just(bearerToken);
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createSignedJwt() throws Exception {

        JWSHeader jwtHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(maskinportenProperties.getKid())
                .build();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .audience(maskinportenProperties.getAudience())
                .issuer(maskinportenProperties.getIssuer())
                .claim("scope", maskinportenProperties.getScope())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date(Clock.systemUTC().millis()))
                .expirationTime(new Date(tokenExpirationTime = (Clock.systemUTC().millis() + JWT_EXPIRATION_TIME)))
                .build();

        PrivateKey privateKey = KeyFactory
                .getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(java.util.Base64
                        .getDecoder()
                        .decode(maskinportenProperties.getPrivateKey())));

        SignedJWT signedJWT = new SignedJWT(jwtHeader, claimsSet);
        signedJWT.sign(new RSASSASigner(privateKey));

        return signedJWT.serialize();
    }

    private boolean isJwtExpired(){
        boolean isExpired = tokenExpirationTime < Clock.systemUTC().millis() - JWT_EXPIRATION_OFFSET;
        if (isExpired) log.debug("🔑 Token is expired.");

        return isExpired;
    }
}