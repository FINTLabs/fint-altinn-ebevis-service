package no.novari.ebevis.factory

import no.fint.altinn.model.AltinnApplication
import no.novari.ebevis.factory.ConsentFactory
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
        authorization.evidenceRequests.last().evidenceCodeName == 'RestanserV2'
        authorization.evidenceRequests.last().requestConsent
    }
}
