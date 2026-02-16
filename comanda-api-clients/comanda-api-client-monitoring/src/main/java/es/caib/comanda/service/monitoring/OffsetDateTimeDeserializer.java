package es.caib.comanda.service.monitoring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    private static final DateTimeFormatter ISO_CUSTOM = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendPattern("[[ ]XXX][XXX][XX][X]")
            .toFormatter();

    private static final DateTimeFormatter ISO_DATE_OPTIONAL_TIME = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .optionalStart()
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalEnd()
            .appendPattern("[[ ]XXX][XXX][XX][X]")
            .parseDefaulting(java.time.temporal.ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(java.time.temporal.ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(java.time.temporal.ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (p.currentToken().isNumeric()) {
            long v = p.getLongValue();
            // Heurística: si és massa gran per ser segons, assumim mil·lisegons
            long seconds = (v > 10_000_000_000L) ? v / 1000L : v;
            long millisRemainder = (v > 10_000_000_000L) ? (v % 1000L) : 0L;
            Instant inst = Instant.ofEpochSecond(seconds, millisRemainder * 1_000_000);
            return OffsetDateTime.ofInstant(inst, ZoneId.systemDefault());
        }
        if (p.currentToken() == JsonToken.START_OBJECT) {
            JsonNode node = p.getCodec().readTree(p);
            int year = node.get("year").asInt();
            int month = node.get("monthValue").asInt();
            int day = node.get("dayOfMonth").asInt();
            int hour = node.has("hour") ? node.get("hour").asInt() : 0;
            int minute = node.has("minute") ? node.get("minute").asInt() : 0;
            int second = node.has("second") ? node.get("second").asInt() : 0;
            int nano = node.has("nano") ? node.get("nano").asInt() : 0;
            ZoneOffset offset = ZoneOffset.UTC;
            if (node.has("offset")) {
                JsonNode offsetNode = node.get("offset");
                if (offsetNode.isObject() && offsetNode.has("totalSeconds")) {
                    offset = ZoneOffset.ofTotalSeconds(offsetNode.get("totalSeconds").asInt());
                } else if (offsetNode.isTextual()) {
                    offset = ZoneOffset.of(offsetNode.asText());
                }
            }
            return OffsetDateTime.of(LocalDateTime.of(year, month, day, hour, minute, second, nano), offset);
        }
        // Delega per a cadenes ISO-8601
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value, ISO_CUSTOM);
        } catch (Exception e) {
            try {
                // Provem a substituir espais per 'T' si falla
                if (value.contains(" ") && !value.contains("T")) {
                    String valueWithT = value.replaceFirst(" ", "T");
                    try {
                        return OffsetDateTime.parse(valueWithT, ISO_CUSTOM);
                    } catch (Exception e2) {
                        // ignore and try next
                    }
                }
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
