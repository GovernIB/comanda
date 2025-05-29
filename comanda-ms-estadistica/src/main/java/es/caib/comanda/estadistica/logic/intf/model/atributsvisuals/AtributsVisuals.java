package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

/**
 * Interfície que representa els atributs visuals d'un widget.
 *
 * @author Límit Tecnologies
 */
public interface AtributsVisuals {

    public String fromAtributsVisuals();
    public AtributsVisuals merge(AtributsVisuals other);

    default <T> T mergeField(T currentValue, T otherValue) {
        return currentValue != null ? currentValue : otherValue;
    }
}