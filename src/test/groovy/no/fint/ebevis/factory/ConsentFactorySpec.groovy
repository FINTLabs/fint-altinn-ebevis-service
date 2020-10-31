package no.fint.ebevis.factory

import no.fint.ebevis.model.AltinnApplication
import spock.lang.Specification

class ConsentFactorySpec extends Specification {

    def "ofTaxiLicenseApplication returns Authorization"() {
        when:
        def authorization = ConsentFactory.ofTaxiLicenseApplication(new AltinnApplication(requestor: 123, subject: 456, archiveReference: 'archive-reference'))

        then:
        authorization.requestor == 123
        authorization.subject == 456
        authorization.consentReference == 'archive-reference'
        authorization.evidenceRequests.first().evidenceCodeName == 'KonkursDrosje'
        !authorization.evidenceRequests.first().requestConsent
        authorization.evidenceRequests.last().evidenceCodeName == 'RestanserDrosje'
        authorization.evidenceRequests.last().requestConsent
    }
}
