package no.novari.ebevis.maskinporten;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class MaskinportenService {

    public static final long JWT_EXPIRATION_OFFSET = 5000;
    public static final long JWT_EXPIRATION_TIME = 120000;
    private final MaskinportenProperties maskinportenProperties;
    private final WebClient webClient;
    private long tokenExpirationTime = 0;
    private String bearerToken;
    private final AtomicReference<Mono<String>> ongoingGeneration = new AtomicReference<>();

    public MaskinportenService(MaskinportenProperties maskinportenProperties, WebClient webClient) {
        this.maskinportenProperties = maskinportenProperties;
        this.webClient = webClient;
    }

    public Mono<String> getBearerToken() {
        return Mono.defer(() ->
            Optional.ofNullable(bearerToken)
                .filter(token -> !isJwtExpired())
                .map(this::useExistingToken)
                .orElseGet(this::generateNewTokenIfNeeded)
        );
    }

    private Mono<String> useExistingToken(String token) {
        log.debug("🔑 Use existing token.");
        return Mono.just(token);
    }

    private Mono<String> generateNewTokenIfNeeded() {
        if (isJwtExpired()) {
            log.debug("🪠 Token is expired, getting ready for new token.");
            ongoingGeneration.set(null);
            bearerToken = null;
        }

        return Optional.ofNullable(ongoingGeneration.get())
            .map(ongoing -> {
                log.debug("🔑 Use ongoing token generation.");
                return ongoing;
            })
            .orElseGet(this::createNewToken);
    }

    private Mono<String> createNewToken() {
        log.debug("🔑 Create new token.");
        Mono<String> newGeneration = createTokenRequest()
                .flatMap(this::handleTokenResponse)
                .doOnError(this::handleError);

        ongoingGeneration.set(newGeneration);
        return newGeneration;
    }

    private Mono<TokenResponse> createTokenRequest() {
        return Mono.fromCallable(this::createSignedJwt)
                .onErrorMap(Exception.class, e -> new RuntimeException("Failed to create signed JWT", e))
                .flatMap(jwt -> webClient
                        .post()
                        .uri(maskinportenProperties.getTokenEndpoint())
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Accept", "application/json")
                        .body(BodyInserters
                                .fromFormData("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                                .with("assertion", jwt))
                        .retrieve()
                        .bodyToMono(TokenResponse.class));
    }

    private Mono<String> handleTokenResponse(TokenResponse response) {
        if (response == null || response.getAccessToken() == null) {
            return Mono.error(new IllegalStateException("Received invalid token response"));
        }
        bearerToken = "Bearer " + response.getAccessToken();
        return Mono.just(bearerToken);
    }

    private void handleError(Throwable e) {
        log.error("Token generation failed: {}", e.getMessage(), e);
        ongoingGeneration.set(null);
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
        return tokenExpirationTime < Clock.systemUTC().millis() - JWT_EXPIRATION_OFFSET;
    }
}
