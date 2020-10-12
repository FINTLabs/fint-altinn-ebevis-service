package no.fint.ebevis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class TaxiLicenseApplication {

    @Id
    private String archiveReference;
}
