package no.fint.ebevis.factory;

import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.ebevis.Authorization;
import no.fint.altinn.model.ebevis.EvidenceRequest;

import java.util.ArrayList;
import java.util.List;

public class ConsentFactory {
    public static final String BANKRUPTCY = "KonkursDrosje";
    public static final String ARREARS = "RestanserV2";

    public static Authorization ofTaxiLicenseApplication(AltinnApplication application) {
        Authorization authorization = new Authorization();
        authorization.setRequestor(Integer.parseInt(application.getRequestor()));
        authorization.setSubject(Integer.parseInt(application.getSubject()));

        EvidenceRequest bankruptcy = new EvidenceRequest();
        bankruptcy.setEvidenceCodeName(BANKRUPTCY);
        bankruptcy.setRequestConsent(false);

        EvidenceRequest arrears = new EvidenceRequest();
        arrears.setEvidenceCodeName(ARREARS);
        arrears.setRequestConsent(true);

        List<EvidenceRequest> evidenceRequests = new ArrayList<>();
        evidenceRequests.add(bankruptcy);
        evidenceRequests.add(arrears);

        authorization.setEvidenceRequests(evidenceRequests);

        authorization.setConsentReference(application.getArchiveReference());

        return authorization;
    }
}
