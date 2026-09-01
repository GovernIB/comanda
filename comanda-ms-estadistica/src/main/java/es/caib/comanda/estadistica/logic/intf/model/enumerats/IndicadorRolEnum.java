package es.caib.comanda.estadistica.logic.intf.model.enumerats;

/**
 * Rol d'un indicador dins la llista d'indicadors d'un widget gràfic, quan cal
 * distingir-los (p. ex. GAUGE_CHART amb DOS_INDICADORS: un indicador de VALOR i
 * un de MAXIM). Per a la resta de tipusDades (UN_INDICADOR, VARIS_INDICADORS,
 * UN_INDICADOR_AMB_DESCOMPOSICIO) aquest camp no s'utilitza i es queda a null.
 *
 * @author Límit Tecnologies
 */
public enum IndicadorRolEnum {
    VALOR,
    MAXIM
}
