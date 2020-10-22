package no.fint.ebevis.factory;

import no.fint.ebevis.model.ebevis.Authorization;
import no.fint.ebevis.model.ebevis.EvidenceRequest;

import java.util.ArrayList;
import java.util.List;

public class ConsentFactory {
    public static final String BANKRUPTCY = "KonkursDrosje";
    public static final String ARREARS = "RestanserDrosje";

    public static Authorization ofTaxiLicenseApplication(String requestor, String subject, String reference) {
        Authorization authorization = new Authorization();
        authorization.setRequestor(Integer.parseInt(requestor));
        authorization.setSubject(Integer.parseInt(subject));

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

        authorization.setConsentReference(reference);

        return authorization;
    }
}
