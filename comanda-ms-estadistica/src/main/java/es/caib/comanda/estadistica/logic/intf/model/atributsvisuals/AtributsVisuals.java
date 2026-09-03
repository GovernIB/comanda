package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Interfície que representa els atributs visuals d'un widget.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AtributsVisuals implements Serializable {

    @Size(max = 8)
    protected String colorText;
    @Size(max = 8)
    protected String colorFons;
    protected Boolean mostrarVora;
    @Size(max = 8)
    protected String colorVora;
    protected Integer ampleVora;
    protected Integer midaFontTitol;
    protected Integer midaFontDescripcio;

    public abstract AtributsVisuals merge(AtributsVisuals other);

    /**
     * Indica si aquest objecte conté cap valor de personalització (algun camp no nul).
     * S'utilitza per determinar si un dashboardItem/widget s'ha de marcar com a "personalitzat".
     */
    public abstract boolean hasOverrides();

    protected boolean hasBaseOverrides() {
        return isNotEmpty(colorText) || isNotEmpty(colorFons) || mostrarVora != null
            || isNotEmpty(colorVora) || ampleVora != null
            || midaFontTitol != null || midaFontDescripcio != null;
    }

    protected static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    <T> T mergeField(T currentValue, T otherValue) {
        return currentValue != null ? currentValue : otherValue;
    }
}
