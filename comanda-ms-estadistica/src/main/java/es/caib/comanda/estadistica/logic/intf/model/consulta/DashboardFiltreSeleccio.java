package es.caib.comanda.estadistica.logic.intf.model.consulta;

import es.caib.comanda.estadistica.logic.intf.model.periode.Periode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Selecció de valors de filtre feta per l'usuari a la capçalera d'un dashboard en el moment de visualitzar-lo
 * (vegeu {@code DashboardFiltre} per a la configuració de quins filtres es mostren). Es transporta com a camp de
 * {@link InformeWidgetParams} en cada crida a l'informe {@code widget_data}, i s'aplica a tots els widgets del
 * dashboard: els valors de dimensió com a filtre addicional, i el període (si s'ha seleccionat) com a sobreescriptura
 * del període propi de cada widget.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFiltreSeleccio implements Serializable {

    /** Codi de dimensió -> valors seleccionats. **/
    private Map<String, List<String>> dimensions;

    /** Període seleccionat, si l'usuari ha sobreescrit el període per defecte dels widgets. **/
    private Periode periode;

    public boolean hasPeriodeOverride() {
        return periode != null && periode.getPeriodeMode() != null;
    }

    /**
     * Representació canònica i estable de la selecció, per incloure-la a la clau de la cache de dades de widgets
     * ({@code ConsultaEstadisticaHelper#getDadesWidget}) - selecions de filtre diferents no s'han de compartir la
     * mateixa entrada de cache.
     */
    public String cacheKey() {
        String dimensionsPart = dimensions == null || dimensions.isEmpty()
                ? ""
                : dimensions.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getValue() != null && !e.getValue().isEmpty())
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue().stream().sorted().collect(Collectors.joining(",")))
                    .collect(Collectors.joining(";"));
        String periodePart = !hasPeriodeOverride()
                ? ""
                : String.join(",",
                        String.valueOf(periode.getPeriodeMode()),
                        String.valueOf(periode.getPresetPeriode()),
                        String.valueOf(periode.getPresetCount()),
                        String.valueOf(periode.getRelatiuPuntReferencia()),
                        String.valueOf(periode.getRelatiuCount()),
                        String.valueOf(periode.getRelatiueUnitat()),
                        String.valueOf(periode.getRelatiuAlineacio()),
                        String.valueOf(periode.getAbsolutTipus()),
                        String.valueOf(periode.getAbsolutDataInici()),
                        String.valueOf(periode.getAbsolutDataFi()),
                        String.valueOf(periode.getAbsolutAnyReferencia()),
                        String.valueOf(periode.getAbsolutAnyValor()),
                        String.valueOf(periode.getAbsolutPeriodeUnitat()),
                        String.valueOf(periode.getAbsolutPeriodeInici()),
                        String.valueOf(periode.getAbsolutPeriodeFi()));
        return dimensionsPart + "|" + periodePart;
    }

}
