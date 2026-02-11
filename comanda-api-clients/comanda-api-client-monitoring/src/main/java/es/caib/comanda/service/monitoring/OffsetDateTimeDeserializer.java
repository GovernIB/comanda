package es.caib.comanda.service.monitoring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
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

    private static final DateTimeFormatter DD_MM_YYYY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DD_MM_YYYY_HH_MM_SS_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        // Cas numèric (epoch segons o mil·lisegons)
        if (token != null && token.isNumeric()) {
            return fromNumeric(p.getLongValue());
        }

        if (token == JsonToken.START_OBJECT) {
            JsonNode node = p.getCodec().readTree(p);

            // Compatibilitat: si és un objecte Temps, el valor real és a node.data
            if (node.has("data")) { // pot ser null també
                return parseNodeAsOffsetDateTime(node.get("data"));
            }

            // Format objecte d'OffsetDateTime "desglossat" (el teu cas original)
            OffsetDateTime odt = tryParseOffsetDateTimePojo(node);
            if (odt != null) {
                return odt;
            }

            return (OffsetDateTime) ctxt.handleUnexpectedToken(OffsetDateTime.class, p);
        }

        // Cas string ISO-8601 / altres variants
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return parseString(value);
    }

    /**
     * Reutilitza la mateixa lògica per parsejar un JsonNode que pot ser:
     * - textual (ISO)
     * - numèric (epoch)
     * - objecte (year/monthValue/dayOfMonth/offset...)
     * - null
     */
    private OffsetDateTime parseNodeAsOffsetDateTime(JsonNode n) {
        if (n == null || n.isNull()) {
            return null;
        }

        if (n.isNumber()) {
            return fromNumeric(n.asLong());
        }

        if (n.isTextual()) {
            String s = n.asText();
            if (s == null || s.trim().isEmpty()) return null;
            return parseString(s);
        }

        if (n.isObject()) {
            OffsetDateTime odt = tryParseOffsetDateTimePojo(n);
            if (odt != null) return odt;

            // Si mai el teu Temps "data" pot venir com { "value": "..."} o similar,
            // aquí és el lloc per afegir-ho, però de moment ho deixem estricte.
        }

        return null;
    }

    private OffsetDateTime fromNumeric(long v) {
        // Heurística: si és massa gran per ser segons, assumim mil·lisegons
        long seconds = (v > 10_000_000_000L) ? v / 1000L : v;
        long millisRemainder = (v > 10_000_000_000L) ? (v % 1000L) : 0L;
        Instant inst = Instant.ofEpochSecond(seconds, millisRemainder * 1_000_000);
        return OffsetDateTime.ofInstant(inst, ZoneId.systemDefault());
    }

    private OffsetDateTime parseString(String value) {
        try {
            return OffsetDateTime.parse(value, ISO_CUSTOM);
        } catch (Exception e) {
            try {
                // Provem a substituir espais per 'T' si falla
                if (value.contains(" ") && !value.contains("T")) {
                    String valueWithT = value.replaceFirst(" ", "T");
                    try {
                        return OffsetDateTime.parse(valueWithT, ISO_CUSTOM);
                    } catch (Exception ignored) {
                        // ignore and try next
                    }
                }
                return OffsetDateTime.parse(value, ISO_DATE_OPTIONAL_TIME);
            } catch (Exception ex) {
                // Fallback for date-only without offset
                try {
                    return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toOffsetDateTime();
                } catch (Exception ex2) {
                    // Try dd/MM/yyyy HH:mm:ss format
                    try {
                        return LocalDateTime.parse(value, DD_MM_YYYY_HH_MM_SS_FORMAT)
                                .atZone(ZoneId.systemDefault())
                                .toOffsetDateTime();
                    } catch (Exception ex3) {
                        // Try dd/MM/yyyy format
                        return LocalDate.parse(value, DD_MM_YYYY_FORMAT)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toOffsetDateTime();
                    }
                }
            }
        }
    }

    /**
     * Cas: {"year":..., "monthValue":..., "dayOfMonth":..., "hour":..., "offset":...}
     */
    private OffsetDateTime tryParseOffsetDateTimePojo(JsonNode node) {
        if (node.has("year") && node.has("monthValue") && node.has("dayOfMonth")) {
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
        return null;
    }
}
