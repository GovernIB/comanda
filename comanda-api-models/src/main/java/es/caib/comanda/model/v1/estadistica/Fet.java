package es.caib.comanda.model.v1.estadistica;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = GenericFet.class)
public interface Fet {
    String getCodi();
    Double getValor();
}
