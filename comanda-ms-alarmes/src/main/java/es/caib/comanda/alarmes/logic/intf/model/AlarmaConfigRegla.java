package es.caib.comanda.alarmes.logic.intf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmaConfigRegla implements Serializable {

    @Builder.Default
    private AlarmaConfigReglaTipusNode tipusNode = AlarmaConfigReglaTipusNode.GRUP;
    private AlarmaConfigReglaOperador operador;
    @Builder.Default
    private List<AlarmaConfigRegla> fills = new ArrayList<>();

    private AlarmaConfigReglaAmbit ambit;
    private AlarmaConfigReglaMetrica metrica;
    private AlarmaConfigReglaComparador comparador;
    private String codiObjecte;
    private BigDecimal valorNumeric;
    @Builder.Default
    private List<String> valorsText = new ArrayList<>();
}
