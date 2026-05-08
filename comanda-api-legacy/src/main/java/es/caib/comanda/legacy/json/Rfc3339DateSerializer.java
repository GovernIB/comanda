package es.caib.comanda.legacy.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import es.caib.comanda.service.management.RFC3339DateFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class Rfc3339DateSerializer extends JsonSerializer<Date> {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new RFC3339DateFormat();
        }
    };

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(DATE_FORMAT.get().format(value));
    }
}
