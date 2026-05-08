package es.caib.comanda.legacy.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import es.caib.comanda.service.management.RFC3339DateFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class LenientDateDeserializer extends JsonDeserializer<Date> {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final String[] DATE_PATTERNS = new String[] {
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ssZ",
            "yyyy-MM-dd",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy"
    };

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();

        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return fromNumeric(p.getLongValue());
        }
        if (token == JsonToken.VALUE_STRING) {
            return parseText(p.getValueAsString());
        }
        if (token == JsonToken.START_OBJECT) {
            JsonNode node = p.getCodec().readTree(p);
            return parseNode(node);
        }
        throw new IOException("Token JSON no suportat per convertir a Date: " + token);
    }

    private Date parseNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.has("data")) {
            return parseNode(node.get("data"));
        }
        if (node.isNumber()) {
            return fromNumeric(node.asLong());
        }
        if (node.isTextual()) {
            return parseText(node.asText());
        }
        if (node.isObject() && node.has("year") && node.has("monthValue") && node.has("dayOfMonth")) {
            return parseExpandedDate(node);
        }
        return null;
    }

    private Date fromNumeric(long value) {
        if (Math.abs(value) < 10000000000L) {
            return new Date(value * 1000L);
        }
        return new Date(value);
    }

    private Date parseText(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.length() == 0) {
            return null;
        }

        Date parsed = tryParse(new RFC3339DateFormat(), text);
        if (parsed != null) {
            return parsed;
        }

        String normalized = normalizeOffset(text);
        for (int i = 0; i < DATE_PATTERNS.length; i++) {
            parsed = tryParse(buildFormat(DATE_PATTERNS[i]), normalized);
            if (parsed != null) {
                return parsed;
            }
        }
        throw new IllegalArgumentException("No s'ha pogut parsejar la data: " + value);
    }

    private Date parseExpandedDate(JsonNode node) {
        Calendar calendar = new GregorianCalendar(UTC);
        calendar.clear();
        calendar.set(Calendar.YEAR, node.get("year").asInt());
        calendar.set(Calendar.MONTH, node.get("monthValue").asInt() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, node.get("dayOfMonth").asInt());
        calendar.set(Calendar.HOUR_OF_DAY, node.has("hour") ? node.get("hour").asInt() : 0);
        calendar.set(Calendar.MINUTE, node.has("minute") ? node.get("minute").asInt() : 0);
        calendar.set(Calendar.SECOND, node.has("second") ? node.get("second").asInt() : 0);
        if (node.has("nano")) {
            calendar.set(Calendar.MILLISECOND, node.get("nano").asInt() / 1000000);
        }

        Date date = calendar.getTime();
        JsonNode offsetNode = node.get("offset");
        if (offsetNode == null || offsetNode.isNull()) {
            return date;
        }

        int totalSeconds = 0;
        if (offsetNode.isObject() && offsetNode.has("totalSeconds")) {
            totalSeconds = offsetNode.get("totalSeconds").asInt();
        } else if (offsetNode.isTextual()) {
            totalSeconds = parseOffsetSeconds(offsetNode.asText());
        }

        return new Date(date.getTime() - (long) totalSeconds * 1000L);
    }

    private int parseOffsetSeconds(String offsetText) {
        if (offsetText == null || offsetText.length() == 0 || "Z".equals(offsetText)) {
            return 0;
        }
        String normalized = offsetText.replace(":", "");
        int sign = normalized.charAt(0) == '-' ? -1 : 1;
        int hours = Integer.parseInt(normalized.substring(1, 3));
        int minutes = normalized.length() >= 5 ? Integer.parseInt(normalized.substring(3, 5)) : 0;
        return sign * ((hours * 60) + minutes) * 60;
    }

    private DateFormat buildFormat(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        sdf.setTimeZone(UTC);
        return sdf;
    }

    private Date tryParse(DateFormat format, String value) {
        try {
            ParsePosition position = new ParsePosition(0);
            Date parsed = format.parse(value, position);
            if (parsed != null && position.getIndex() == value.length()) {
                return parsed;
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeOffset(String value) {
        if (value.endsWith("Z")) {
            return value.substring(0, value.length() - 1) + "+0000";
        }
        if (value.length() > 5) {
            char sign = value.charAt(value.length() - 6);
            if ((sign == '+' || sign == '-') && value.charAt(value.length() - 3) == ':') {
                return value.substring(0, value.length() - 3) + value.substring(value.length() - 2);
            }
        }
        if (value.length() > 5) {
            char sign = value.charAt(value.length() - 5);
            if ((sign == '+' || sign == '-') && value.charAt(value.length() - 3) == ':') {
                return value.substring(0, value.length() - 3) + value.substring(value.length() - 2);
            }
        }
        return value;
    }
}
