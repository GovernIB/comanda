package es.caib.comanda.estadistica.logic.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class DoubleJsonMapConverter implements AttributeConverter<Map<String, Double>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Double> attribute) {
        try {
            return attribute != null ? objectMapper.writeValueAsString(attribute) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error convertint map a JSON", e);
        }
    }

    @Override
    public Map<String, Double> convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ?
                    objectMapper.readValue(dbData, new TypeReference<Map<String, Double>>() {}) :
                    null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error convertint JSON a map", e);
        }
    }
}