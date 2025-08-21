package no.novari.ebevis.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import no.fint.altinn.model.kafka.KafkaEvidenceConsentRequest;
import no.novari.ebevis.repository.AltinnApplicationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsentRequestConsumer {

    private final AltinnApplicationRepository repository;

    public ConsentRequestConsumer(AltinnApplicationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${fint.kafka.topic.consent-request}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(KafkaEvidenceConsentRequest evidenceRequest) {

        log.info("Congratulations! 🎉 You received a new InstanceActor with altinnReference {} and organizationNumber {}, orgId {}",
                evidenceRequest.getAltinnReference(),
                evidenceRequest.getOrganizationNumber(), evidenceRequest.getFintOrgId());

        AltinnApplication application = new AltinnApplication();
        application.setArchiveReference(evidenceRequest.getAltinnReference());
        application.setRequestor(evidenceRequest.getCountyOrganizationNumber());
        application.setSubject(evidenceRequest.getOrganizationNumber());
        application.setStatus(AltinnApplicationStatus.NEW);
        application.setFintOrgId(evidenceRequest.getFintOrgId());

        repository.save(application);
    }
}
