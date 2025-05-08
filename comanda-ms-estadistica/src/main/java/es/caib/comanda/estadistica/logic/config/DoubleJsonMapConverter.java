package es.caib.comanda.estadistica.logic.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;

/**
 * Aquesta classe és un convertidor de JPA que s'encarrega de gestionar la conversion de valors entre una propietat de tipus
 * Map<String, Double> en l'entitat i una columna JSON de la base de dades.
 * Utilitza la biblioteca Jackson per a la serialització i deserialització de dades JSON.
 *
 * La conversió bidireccional consisteix en:
 * - De Map<String, Double> a String JSON (quan es guarda en la base de dades).
 * - De String JSON a Map<String, Double> (quan es recupera des de la base de dades).
 *
 * Qualsevol error durant el procés de serialització o deserialització generarà una excepció d'execució RuntimeException.
 *
 * Aquesta classe està anotada amb @Converter(autoApply = true), el que implica que s'aplicarà automàticament a qualsevol atribut
 * de tipus Map<String, Double> configurat amb la conversió a nivell de JPA.
 *
 * @author Límit Tecnologies
 */
@Converter(autoApply = true)
public class DoubleJsonMapConverter implements AttributeConverter<Map<String, Double>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converteix un atribut de tipus Map<String, Double> a un valor de cadena JSON per emmagatzemar-lo a la base de dades.
     *
     * @param attribute l'atribut de tipus Map<String, Double> que es vol convertir. Pot ser null.
     * @return una cadena JSON representant el mapa proporcionat, o null si l'atribut d'entrada és null.
     * @throws RuntimeException si es produeix un error durant la serialització del mapa a cadena JSON.
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Double> attribute) {
        try {
            return attribute != null ? objectMapper.writeValueAsString(attribute) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error convertint map a JSON", e);
        }
    }

    /**
     * Converteix un valor de cadena JSON emmagatzemat a la base de dades a un atribut de tipus Map<String, Double>.
     *
     * @param dbData la cadena JSON recuperada de la base de dades que representa un mapa. Pot ser null.
     * @return un mapa de tipus Map<String, Double> obtingut a partir de la cadena JSON, o null si l'entrada és null.
     * @throws RuntimeException si es produeix un error durant la deserialització de la cadena JSON al mapa.
     */
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