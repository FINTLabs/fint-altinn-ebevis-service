package no.fint.ebevis.util

import no.fint.ebevis.model.ebevis.Authorization
import no.fint.ebevis.model.ebevis.EvidenceParameter
import no.fint.ebevis.model.ebevis.EvidenceRequest
import no.fint.ebevis.model.ebevis.LegalBasis
import no.fint.ebevis.model.ebevis.vocab.ParamType
import no.fint.ebevis.model.ebevis.vocab.Type

import java.time.OffsetDateTime

class ObjectFactory {

    static Authorization newAuthorization() {
        return new Authorization(
                requestor: 123,
                subject: 456,
                evidenceRequests: [
                        new EvidenceRequest(
                                evidenceCodeName: 'evidenceCodeName',
                                parameters: [
                                        new EvidenceParameter(
                                                evidenceParamName: 'evidenceParamName',
                                                paramType: ParamType.STRING,
                                                required: true,
                                                value: 'value',
                                        )
                                ],
                                legalBasisId: 'legalBasisId',
                                legalBasisReference: 'legalBasisReference',
                                requestConsent: true,
                        )
                ],
                legalBasisList: [
                        new LegalBasis(
                                id: 'id',
                                type: Type.ESPD,
                                content: 'content'
                        )
                ],
                externalReference: 'externalReference',
                consentReference: 'consentReference',
                validTo: OffsetDateTime.parse('2020-01-01T00:00:00Z')
        )
    }
}
