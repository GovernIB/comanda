package es.caib.comanda.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class ConvertOpenApiYamlToJson {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Ús: YamlToJsonConverter <input.yaml> <output.json>");
            System.exit(1);
        }

        File input = new File(args[0]);
        File output = new File(args[1]);

        // ObjectMapper per YAML
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(input, Object.class);

        // ObjectMapper per JSON
        ObjectMapper jsonWriter = new ObjectMapper();
        jsonWriter.writerWithDefaultPrettyPrinter().writeValue(output, obj);

        System.out.println("Conversió completada: " + output.getAbsolutePath());
    }
}
