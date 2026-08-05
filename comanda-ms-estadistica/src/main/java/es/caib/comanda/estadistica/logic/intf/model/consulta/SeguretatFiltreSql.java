package es.caib.comanda.estadistica.logic.intf.model.consulta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Restricció de seguretat a aplicar a una consulta de fets, en funció dels permisos d'entitat/òrgan gestor de
 * l'usuari (vegeu DashboardSeguretatHelper). A diferència del filtre de dimensions habitual (que és sempre un AND
 * entre dimensions), aquesta restricció és una unió (OR) entre com a màxim dues dimensions - l'usuari ha de veure
 * les dades si l'entitat ÉS una de les permeses, O si l'òrgan (o un dels seus ascendents) ho és.
 * <p>
 * {@code null} (o cap dels dos camps establert) vol dir "sense restricció" (usuari administrador/consulta, o l'app
 * no té cap d'aquestes dues dimensions configurades). Si es construeix amb algun dels dos camps establert però amb
 * una llista de valors buida, el dialecte ha de denegar totes les files (fail-closed) - vegeu
 * FetRepositoryDialect.generateDimensionConditions.
 *
 * @author Límit Tecnologies
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguretatFiltreSql {

    private String dimensioEntitatCodi;
    private List<String> valorsEntitatPermesos;

    private String dimensioOrganCodi;
    private List<String> valorsOrganPermesos;

    public boolean isActiva() {
        return dimensioEntitatCodi != null || dimensioOrganCodi != null;
    }

}
