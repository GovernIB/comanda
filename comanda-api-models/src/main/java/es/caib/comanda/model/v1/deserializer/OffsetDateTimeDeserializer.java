package es.caib.comanda.model.v1.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    private static final DateTimeFormatter ISO_CUSTOM = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendPattern("[XXX][XX][X]")
            .toFormatter();

    private static final DateTimeFormatter ISO_DATE_OPTIONAL_TIME = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .optionalStart()
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalEnd()
            .appendPattern("[[XXX][XX][X]]")
            .parseDefaulting(java.time.temporal.ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(java.time.temporal.ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(java.time.temporal.ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

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
        String value = p.getValueAsString();
        try {
            return OffsetDateTime.parse(value, ISO_CUSTOM);
        } catch (Exception e) {
            try {
                return OffsetDateTime.parse(value, ISO_DATE_OPTIONAL_TIME);
            } catch (Exception ex) {
                // Fallback for date-only without offset
                return java.time.LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toOffsetDateTime();
            }
        }
    }
}
