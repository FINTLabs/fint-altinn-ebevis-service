package no.novari.ebevis.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.MediaType;

import java.io.IOException;

@JsonComponent
public class MediaTypeSerializer extends JsonSerializer<MediaType> {

    @Override
    public void serialize(MediaType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getType() + "/" + value.getSubtype());
    }
}
