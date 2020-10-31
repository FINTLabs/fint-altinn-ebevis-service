package no.fint.ebevis.model.ebevis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.ebevis.model.ebevis.vocab.NotificationType;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class Notification {
    @JsonProperty("notificationType")
    private NotificationType notificationType;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("description")
    private String description;

    @JsonProperty("recipientCount")
    private Integer recipientCount;

    @JsonProperty("date")
    private OffsetDateTime date;
}
