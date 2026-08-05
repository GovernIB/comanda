package es.caib.comanda.estadistica.logic.intf.model.consulta;

import lombok.Builder;
import lombok.Getter;

/**
 * Resultat de resoldre la seguretat d'accés a les dades d'un widget per a l'usuari actual
 * (vegeu DashboardSeguretatHelper).
 *
 * @author Límit Tecnologies
 */
@Getter
@Builder
public class SeguretatDadesResultat {

    /** L'usuari és administrador o de consulta: veu totes les dades sense cap restricció. */
    private boolean exempt;

    /**
     * L'usuari no té cap permís d'entitat ni d'òrgan enlloc del sistema - no s'ha d'executar cap consulta;
     * el widget ha de mostrar un missatge informatiu en lloc de dades (buides).
     */
    private boolean sensePermisos;

    /**
     * Restricció a aplicar a la consulta d'aquest widget. Null si `exempt` és cert. Pot ser una restricció
     * "activa" amb llistes de valors buides si l'usuari té permisos però cap no aplica a aquesta app en concret
     * (en aquest cas, FetRepositoryDialect ha de denegar totes les files).
     */
    private SeguretatFiltreSql filtreSql;

}
