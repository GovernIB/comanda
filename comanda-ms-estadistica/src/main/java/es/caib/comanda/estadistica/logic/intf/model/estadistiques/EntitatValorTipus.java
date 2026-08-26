package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

/**
 * Indica com s'ha d'interpretar el valor d'una dimensió de tipus {@link TipusDimensioEnum#ENTITAT} per relacionar-lo
 * amb una {@code EntitatEntity} definida a Comanda. Algunes apps envien el codi intern de l'entitat (Entitat.codi),
 * d'altres envien directament el seu codi Dir3 (Entitat.codiDir3).
 *
 * @author Límit Tecnologies
 */
public enum EntitatValorTipus {
    /** El valor de la dimensió és el codi intern de l'entitat a Comanda (Entitat.codi). Per defecte. */
    CODI,
    /** El valor de la dimensió és el codi Dir3 de l'entitat (Entitat.codiDir3). */
    CODI_DIR3,
    /** El valor de la dimensió és el nom de l'entitat (Entitat.nom). */
    NOM,
    /** El valor de la dimensió és el CIF de l'entitat (Entitat.cif). */
    CIF,
    /**
     * El valor de la dimensió no coincideix amb cap camp de l'entitat: la relació s'ha d'indicar manualment per
     * a cada valor (vegeu {@code DimensioValorEntity.entitatMapejada}). No s'intenta cap coincidència automàtica.
     */
    MANUAL
}
