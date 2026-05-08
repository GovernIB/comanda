package es.caib.comanda.service.management;

import es.caib.comanda.legacy.json.LenientDateDeserializer;

import java.util.Date;

/**
 * Alias històric mantingut per compatibilitat de codi font.
 */
public class OffsetDateTimeDeserializer extends LenientDateDeserializer {
    public Class<Date> handledType() {
        return Date.class;
    }
}
