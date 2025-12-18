package es.caib.comanda.model.v1.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken().isNumeric()) {
            long v = p.getLongValue();
            // Heurística: si és massa gran per ser segons, assumim mil·lisegons
            long seconds = (v > 10_000_000_000L) ? v / 1000L : v;
            long millisRemainder = (v > 10_000_000_000L) ? (v % 1000L) : 0L;
            Instant inst = Instant.ofEpochSecond(seconds, millisRemainder * 1_000_000);
            return OffsetDateTime.ofInstant(inst, ZoneId.systemDefault());
        }
        // Delega per a cadenes ISO-8601
        return OffsetDateTime.parse(p.getValueAsString());
    }
}
