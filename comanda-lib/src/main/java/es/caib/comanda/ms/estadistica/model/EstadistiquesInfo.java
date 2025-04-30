package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EstadistiquesInfo {
    private String codi;
    private String versio;
    private Date data;
    private List<DimensioDesc> dimensions;
    private List<IndicadorDesc> indicadors;
}
