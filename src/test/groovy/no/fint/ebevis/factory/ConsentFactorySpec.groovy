package no.fint.ebevis.factory

import spock.lang.Specification

class ConsentFactorySpec extends Specification {

    def "ofTaxiLicenseApplication returns Authorization"() {
        when:
        def authorization = ConsentFactory.ofTaxiLicenseApplication('123', '456', 'consent-reference')

        then:
        authorization.requestor == 123
        authorization.subject == 456
        authorization.consentReference == 'consent-reference'
        authorization.evidenceRequests.first().evidenceCodeName == 'KonkursDrosje'
        !authorization.evidenceRequests.first().requestConsent
        authorization.evidenceRequests.last().evidenceCodeName == 'RestanserDrosje'
        authorization.evidenceRequests.last().requestConsent
    }
}
