package no.fint.ebevis.model;

import lombok.Data;
import no.fint.ebevis.model.ebevis.Evidence;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Document
public class AltinnApplication {
    @Id
    private String archiveReference;
    private String requestor;
    private String subject;
    private String serviceCode;
    private Integer languageCode;
    private Form form;
    private String accreditationId;
    private AltinnApplicationStatus status;
    private List<Evidence> evidence = new ArrayList<>();
    private List<Attachment> attachments = new ArrayList<>();
    private Map<String, AltinnApplicationConsentStatus> consents = new HashMap<>();

    @Version
    private long version;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @CreatedDate
    private LocalDateTime createdDate;

    @Data
    public static class Form {
        private String formData;
        private byte[] formDataPdf;
    }

    @Data
    public static class Attachment {
        private Integer attachmentId;
        private byte[] attachmentData;
        private String attachmentType;
        private String attachmentTypeName;
        private String attachmentTypeNameLanguage;
    }
}
