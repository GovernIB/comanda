package es.caib.comanda.estadistica.logic.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;

/**
 * Aquesta classe és un convertidor de JPA que fa possible gestionar la conversió entre un atribut de tipus Map<String, String>
 * en l'entitat i una columna JSON en la base de dades.
 * Utilitza la biblioteca Jackson per a serialitzar i deserialitzar les dades JSON.
 *
 * La funcionalitat principal inclou:
 * - Convertir un Map<String, String> en una representació JSON (per ser emmagatzemat a la base de dades).
 * - Convertir una cadena JSON en un Map<String, String> (quan es carrega des de la base de dades).
 *
 * Si es produeix un error durant la serialització o deserialització, es llançarà una excepció d'execució RuntimeException.
 *
 * Aquesta classe està anotada amb @Converter(autoApply = true), el que significa que aquest convertidor s'aplicarà automàticament
 * a qualsevol atribut de tipus Map<String, String> utilitzat en les entitats JPA.
 *
 * @author Límit Tecnologies
 */
@Converter(autoApply = true)
public class StringJsonMapConverter implements AttributeConverter<Map<String, String>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converteix un atribut de tipus Map<String, String> a un valor de cadena JSON per emmagatzemar-lo a la base de dades.
     *
     * @param attribute l'atribut de tipus Map<String, String> que es vol convertir. Pot ser null.
     * @return una cadena JSON que representa el mapa proporcionat, o null si l'atribut d'entrada és null.
     * @throws RuntimeException si es produeix un error durant la serialització del mapa a cadena JSON.
     */
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            return attribute != null ? objectMapper.writeValueAsString(attribute) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error convertint map a JSON", e);
        }
    }

    /**
     * Converteix una cadena JSON emmagatzemada a la base de dades a un atribut de tipus Map<String, String>.
     *
     * @param dbData la cadena JSON recuperada de la base de dades que representa un mapa. Pot ser null.
     * @return un mapa de tipus Map<String, String> obtingut a partir de la cadena JSON, o null si l'entrada és null.
     * @throws RuntimeException si es produeix un error durant la deserialització de la cadena JSON al mapa.
     */
    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ? 
                   objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {}) :
                   null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error convertint JSON a map", e);
        }
    }
}