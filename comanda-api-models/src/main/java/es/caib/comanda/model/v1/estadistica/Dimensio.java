package es.caib.comanda.model.v1.estadistica;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = GenericDimensio.class)
public interface Dimensio {
    String getCodi();
    String getValor();
}
