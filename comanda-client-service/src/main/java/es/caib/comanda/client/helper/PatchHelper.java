package es.caib.comanda.client.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.json.Json;
import javax.json.JsonPatchBuilder;
import javax.json.JsonValue;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PatchHelper {

//    @SneakyThrows
    public static void replaceProperty(
            JsonPatchBuilder jpb,
            String propertyName,
            Object finalValue) {

        if (finalValue == null) {
            jpb.replace("/" + propertyName, JsonValue.NULL);
        } else if (finalValue instanceof String) {
            jpb.replace("/" + propertyName, (String)finalValue);
        } else if (finalValue instanceof Number) {
            jpb.replace("/" + propertyName, finalValue.toString());
        } else if (finalValue instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            jpb.replace("/" + propertyName, sdf.format((Date) finalValue));
        } else if (finalValue instanceof Boolean) {
            jpb.replace("/" + propertyName, (Boolean) finalValue);
        } else if (List.class.isAssignableFrom(finalValue.getClass())) {
            replaceArrayProperty(jpb, propertyName, (List) finalValue);
        } else {
            replaceObjectProperty(jpb, propertyName, finalValue);
        }
    }

//    @SneakyThrows
    public static boolean replaceProperty(
            JsonPatchBuilder jpb,
            String propertyName,
            Object originalValue,
            Object finalValue) {

        if (!Objects.equals(originalValue, finalValue)) {
            replaceProperty(jpb, propertyName, finalValue);
            return true;
        }
        return false;
    }

    /** Transforma un objecte de tipus JsonPatshBuilder a un objecte JsonNode per
     * poder enviar-lo com a paràmetre en les crides patch.
     *
     * @param jpb
     * @return
     */
    public static JsonNode toJsonNode(JsonPatchBuilder jpb) {
        JsonNode patchJson = null;
        try {
            patchJson = new ObjectMapper().readTree(jpb.build().toString());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return patchJson;
    }


    // TODO: Replace array tenint en compte el tipus d'objecte de l'array
    @SneakyThrows
    private static void replaceArrayProperty(
            JsonPatchBuilder jpb,
            String propertyName,
            List finalValue) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonArray = objectMapper.writeValueAsString(finalValue);
        jpb.replace("/" + propertyName, jsonArray);
    }

    // TODO: Comprovar si funciona
    @SneakyThrows
    private static void replaceObjectProperty(
            JsonPatchBuilder jpb,
            String propertyName,
            Object finalObject) {

        if (finalObject == null) {
            jpb.replace("/" + propertyName, JsonValue.NULL);
        }

        JsonPatchBuilder ojpb = Json.createPatchBuilder();

        Field[] fields = finalObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            String oPropertyName = field.getName();
            Object finalValue = field.get(finalObject);

            if (finalValue == null) {
                ojpb.replace("/" + propertyName, JsonValue.NULL);
            } else {
                Class<?> fieldType = field.getType();

                if (List.class.isAssignableFrom(fieldType)) {
                    replaceArrayProperty(ojpb, oPropertyName, (List) finalValue);
                } else {
                    replaceProperty(ojpb, oPropertyName, finalValue);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonObject = objectMapper.writeValueAsString(objectMapper.readTree(ojpb.build().toString()));
                jpb.replace("/" + propertyName, jsonObject);
            }
        }
    }

}
