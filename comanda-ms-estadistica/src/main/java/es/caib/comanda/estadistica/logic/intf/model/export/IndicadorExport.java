package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.CompactacioEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.model.v1.estadistica.Format;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * Classe per exportar un Indicador utilitzat en un o més widgets d'un dashboard (vegeu {@link DashboardExport#getIndicadors()}).
 *
 * S'inclou al dashboard exportat perquè, en importar-lo a un altre entorn, els indicadors de tipus FORMULA
 * (que es creen i s'editen manualment des de la pantalla d'Indicadors, a diferència dels SIMPLE que es
 * sincronitzen automàticament des de les apps) es puguin crear si encara no existeixen a l'entorn destí, en
 * lloc de bloquejar la importació.
 *
 * L'indicador es referencia i s'identifica pel seu {@code codi} dins del seu entornApp ({@code entornCodi} +
 * {@code appCodi}), no per id, ja que els id no es preserven entre entorns/instàncies.
 *
 * @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorExport implements Serializable {

    @NotBlank
    @Size(max = 32)
    private String codi;

    @NotBlank
    @Size(max = 64)
    private String nom;

    @Size(max = 1024)
    private String descripcio;

    private String entornCodi;
    private String appCodi;

    private Format format;

    @NotNull
    private IndicadorTipus tipus;

    private Boolean compactable;
    private CompactacioEnum tipusCompactacio;
    private String indicadorComptadorPerMitjanaCodi;

    // Fórmula (només rellevant quan tipus == FORMULA): vegeu IndicadorEntity#formula.
    @Valid
    private List<IndicadorFormulaTermeExport> formula;

}
