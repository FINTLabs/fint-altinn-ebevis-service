package no.fint.ebevis.configuration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jdk.nashorn.internal.parser.Token;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;

@Configuration
public class MaskinportenConfiguration {

    private final MaskinportenProperties maskinportenProperties;

    public MaskinportenConfiguration(MaskinportenProperties maskinportenProperties) {
        this.maskinportenProperties = maskinportenProperties;
    }

    public Mono<String> getAccessToken() {
        try {

            return WebClient.builder().baseUrl(maskinportenProperties.getTokenEndpoint()).build()
                    .post()
                    .body(BodyInserters
                            .fromFormData("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                            .with("assertion", createSignedJwt()))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .onErrorMap(e -> new RuntimeException("Error fetching token", e))
                    .flatMap(response -> Mono.just(response.getAccessToken()));

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
                .expirationTime(new Date(Clock.systemUTC().millis() + 120000))
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



}

