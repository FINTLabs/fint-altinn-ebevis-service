package no.fint.ebevis.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Document
public class AltinnApplication {
    @Id
    private String archiveReference;
    private String accreditationId;
    private String requestor;
    private String subject;
    private String serviceCode;
    private Integer languageCode;
    private AltinnApplicationStatus status;
    private Form form;
    private Map<Integer, Attachment> attachments = new HashMap<>();
    private Map<String, Consent> consents = new HashMap<>();

    @Version
    private long version;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @CreatedDate
    private LocalDateTime createdDate;

    @Data
    public static class Form {
        private String formData;
    }

    @Data
    public static class Attachment {
        private Integer attachmentId;
        private String attachmentType;
        private String attachmentTypeName;
        private String attachmentTypeNameLanguage;
    }

    @Data
    public static class Consent {
        private ConsentStatus status;
        private String evidenceCodeName;
        private String documentId;
    }
}
