package no.fint.ebevis.model.ebevis;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EvidenceStatusCode   {
  @JsonProperty("description")
  private String description;

  @JsonProperty("code")
  private Integer code;

  @JsonProperty("retryAt")
  private ZonedDateTime retryAt;

  public EvidenceStatusCode(String description, Integer code) {
    this.description = description;
    this.code = code;
  }
}

