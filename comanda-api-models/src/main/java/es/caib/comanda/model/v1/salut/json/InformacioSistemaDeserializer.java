package es.caib.comanda.model.v1.salut.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.model.v1.salut.DetallSalut;
import es.caib.comanda.model.v1.salut.InformacioSistema;

import java.io.IOException;

/**
 * Deserialitzador retrocompatible per al camp informacioSistema de SalutInfo.
 * Accepta tant el nou format (objecte InformacioSistema) com l'antic (llista de DetallSalut).
 */
public class InformacioSistemaDeserializer extends JsonDeserializer<InformacioSistema> {

    @Override
    public InformacioSistema deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_NULL) return null;

        ObjectMapper mapper = (ObjectMapper) p.getCodec();

        if (t == JsonToken.START_OBJECT) {
            // Nou format: objecte amb camps fixos
            return mapper.readValue(p, InformacioSistema.class);
        } else if (t == JsonToken.START_ARRAY) {
            // Format antic: llista de DetallSalut amb codis coneguts
            JsonNode node = mapper.readTree(p);
            InformacioSistema info = new InformacioSistema();
            for (JsonNode item : node) {
                DetallSalut d = mapper.convertValue(item, DetallSalut.class);
                if (d == null || d.getCodi() == null) continue;
                String codi = d.getCodi();
                String valor = d.getValor();
                switch (codi) {
                    case "PRC": // Processadors
                        try { info.setProcessadors(valor != null ? Integer.parseInt(valor) : null); } catch (NumberFormatException ignored) {}
                        break;
                    case "LAVG": // Càrrega del sistema
                        info.setCarregaSistema(valor);
                        break;
                    case "SCPU": // CPU sistema
                        info.setCpuSistema(valor);
                        break;
                    case "MET": // Memòria total
                        info.setMemoriaTotal(valor);
                        break;
                    case "MED": // Memòria disponible
                        info.setMemoriaDisponible(valor);
                        break;
                    case "EDT": // Espai disc total
                        info.setEspaiDiscTotal(valor);
                        break;
                    case "EDL": // Espai disc lliure
                        info.setEspaiDiscLliure(valor);
                        break;
                    case "SO": // Sistema operatiu
                        info.setSistemaOperatiu(valor);
                        break;
                    case "ST": // Data d'arrencada
                        info.setDataArrencada(valor);
                        break;
                    case "UT": // Temps funcionant
                        info.setTempsFuncionant(valor);
                        break;
                    default:
                        // Ignorar codis desconeguts
                        break;
                }
            }
            return info;
        } else {
            // Qualsevol altre: intenta convertir directament a objecte
            return mapper.readValue(p, InformacioSistema.class);
        }
    }
}
