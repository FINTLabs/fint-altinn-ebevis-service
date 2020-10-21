package no.fint.ebevis.client;

import no.fint.ebevis.factory.ConsentFactory;
import no.fint.ebevis.model.ebevis.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    private final DataAltinnClient dataAltinnClient;
    private final DataAltinnMetadataClient dataAltinnMetadataClient;

    public TestController(DataAltinnClient dataAltinnClient, DataAltinnMetadataClient dataAltinnMetadataClient) {
        this.dataAltinnClient = dataAltinnClient;
        this.dataAltinnMetadataClient = dataAltinnMetadataClient;
    }

    @GetMapping
    public Mono<ResponseEntity<Accreditation>> test() {
        Authorization authorization = ConsentFactory.ofTaxiLicenseApplication(921693230, 998997801, "reference");

        return dataAltinnClient.createAccreditation(authorization);
    }
}
